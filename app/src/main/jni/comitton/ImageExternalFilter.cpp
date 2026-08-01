#include <malloc.h>
#include <string.h>
#include <math.h>
#include <pthread.h>
#include <algorithm>
#include <vector>
#include <cmath>
#include <arm_neon.h>

#ifdef _WIN32
#include <stdio.h>
#else
#include <android/log.h>
#endif

#include "Image.h"

extern LONG	**gLinesPtr[];
extern LONG	**gSclLinesPtr[];
extern int	gCancel[];
extern int	gMaxThreadNum;

// 2層分のポインタを確保
LONG** gWorkLinesPtr = nullptr;

// ============================================================================
// パイプライン設定構造体 & 定義
// ============================================================================
enum class FilterStage1 { 
	NONE, 
	GAUSSIAN_3X3,
	GAUSSIAN_5X5,
	GAUSSIAN_7X7,
	MEDIAN_3X3,
	MEDIAN_5X5,
	MEDIAN_7X7,
	MAX_3X3,
	MAX_5X5,
	MIN_3X3,
	MIN_5X5
};

enum class FilterStage2 { 
	NONE,
	BILATERAL,
	GUIDED,
	ANISOTROPIC_DIFFUSION,
	NL_MEANS,
	WAVELET_THRESHOLD
};

enum class FilterStage3 { 
	NONE,
	OPENING,
	CLOSING,
	TOP_HAT,
	BLACK_HAT
};

enum class FilterStage4 { 
	NONE,
	S_CURVE,
	ADAPTIVE_THRESHOLD,
	OTSU_THRESHOLD
};

struct PipelineConfig {
	FilterStage1 stage1 = FilterStage1::NONE;
	FilterStage2 stage2 = FilterStage2::NONE;
	FilterStage3 stage3 = FilterStage3::NONE;
	FilterStage4 stage4 = FilterStage4::NONE;
	// パラメータ
	int radius = 3;
	float sigma_spatial = 3.0f;
	float sigma_range = 30.0f;

	int guided_r = 4;
	float guided_eps = 0.01f;

	int ad_iterations = 3;
	float ad_k = 15.0f;
	float ad_lambda = 0.15f;
	// NL-Means パラメータ
	// 探索窓サイズ
	int nlm_search_window = 7;
	// 類似度比較パッチサイズ
	int nlm_patch_size = 3;	
	// フィルター強度パラメータ
	float nlm_h = 15.0f;
	// Wavelet パラメータ
	// しきい値
	uint8_t wavelet_threshold = 15;

	float scurve_gain = 10.0f;
	float scurve_cutoff = 0.5f;

	int adaptive_window_size = 15;
	int adaptive_c = 10;

	// 事前計算用の結果保持
	uint8_t otsu_threshold = 128;
};

struct ThreadParam {
	int stindex;
	int edindex;
	int OrgWidth;
	int OrgHeight;
	int index;
	PipelineConfig config;
	pthread_barrier_t* pBarrier;
};

struct ColorRGB {
	uint8_t r;
	uint8_t g;
	uint8_t b;
};

static const int GAUSS_KERNEL_3x3[3][3] = {
	{ 1, 2, 1 },
	{ 2, 4, 2 },
	{ 1, 2, 1 }
};

static const int GAUSS_KERNEL_5x5[5][5] = {
	{ 1,  4,  7,  4, 1 },
	{ 4, 16, 26, 16, 4 },
	{ 7, 26, 41, 26, 7 },
	{ 4, 16, 26, 16, 4 },
	{ 1,  4,  7,  4, 1 }
};

static const int GAUSS_KERNEL_7x7[7][7] = {
	{ 1,  4,  7, 10,  7,  4, 1 },
	{ 4, 12, 26, 33, 26, 12, 4 },
	{ 7, 26, 51, 66, 51, 26, 7 },
	{10, 33, 66, 88, 66, 33, 10 },
	{ 7, 26, 51, 66, 51, 26, 7 },
	{ 4, 12, 26, 33, 26, 12, 4 },
	{ 1,  4,  7, 10,  7,  4, 1 }
};

// 輝度と画素をセットで扱う構造体(RGBの色ズレを防ぐため)
struct PixelLuma {
	ColorRGB color;
	uint8_t luma;
	// std::min_element / max_element で比較できるようにする
	bool operator<(const PixelLuma& other) const {
		return luma < other.luma;
	}
};

// ============================================================================
// LUT(ルックアップテーブル)管理 & 事前計算
// ============================================================================
static uint8_t g_SCurveLUT[256];
static float   g_AD_DiffLUT[512];

static void PrecomputeLUTs(PipelineConfig& config, LONG** src, int width, int height) {
	if (config.stage4 == FilterStage4::S_CURVE) {
		// S字カーブのLUT処理
		for (int i = 0; i < 256; i++) {
			float norm = i / 255.0f;
			float sig = 1.0f / (1.0f + std::exp(-config.scurve_gain * (norm - config.scurve_cutoff)));
			g_SCurveLUT[i] = (uint8_t)std::clamp(sig * 255.0f, 0.0f, 255.0f);
		}
	}

	if (config.stage2 == FilterStage2::ANISOTROPIC_DIFFUSION) {
		// 異方性拡散のLUT処理
		float k2 = config.ad_k * config.ad_k;
		for (int diff = -255; diff <= 255; diff++) {
			float c = std::exp(-(diff * diff) / k2);
			g_AD_DiffLUT[diff + 255] = config.ad_lambda * c * diff;
		}
	}

	if (config.stage4 == FilterStage4::OTSU_THRESHOLD && src != nullptr) {
		// 大津の二値化の事前計算
		int histogram[256] = {0};
		int totalPixels = width * height;
		// ヒストグラムを求める
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				LONG color = src[y][x];
				uint8_t r = (color >> 16) & 0xFF;
				uint8_t g = (color >> 8) & 0xFF;
				uint8_t b = color & 0xFF;
				uint8_t gray = static_cast<uint8_t>((r * 38 + g * 75 + b * 15) >> 7);;
				histogram[gray]++;
			}
		}

		float sum = 0;
		for (int i = 0; i < 256; i++) sum += i * histogram[i];

		float sumB = 0;
		int wB = 0;
		float maxVar = 0.0f;
		uint8_t bestThresh = 128;
		// しきい値を求める
		for (int t = 0; t < 256; t++) {
			wB += histogram[t];
			if (wB == 0) continue;
			int wF = totalPixels - wB;
			if (wF == 0) break;

			sumB += (float)(t * histogram[t]);
			float mB = sumB / wB;
			float mF = (sum - sumB) / wF;

			float varBetween = (float)wB * (float)wF * (mB - mF) * (mB - mF);
			if (varBetween > maxVar) {
				maxVar = varBetween;
				bestThresh = (uint8_t)t;
			}
		}
		config.otsu_threshold = bestThresh;
	}

}

// ============================================================================
// インライン・ヘルパー関数
// ============================================================================
inline ColorRGB GetRGB(LONG color) {
	ColorRGB rgb;
	rgb.r = (color >> 16) & 0xFF;
	rgb.g = (color >> 8)  & 0xFF;
	rgb.b =  color		& 0xFF;
	return rgb;
}

inline LONG MakeColorRGB(uint8_t r, uint8_t g, uint8_t b) {
	return ((LONG)r << 16) | ((LONG)g << 8) | (LONG)b;
}

inline LONG GetPixelSafe(LONG** src, int x, int y, int width, int height) {
	int clpY = std::clamp(y, 0, height - 1);
	int clpX = std::clamp(x, 0, width - 1);
	return src[clpY][clpX];
}

// 輝度計算のインライン関数(Rec. 601 / 709 相当の係数を使用)
inline uint8_t CalcLuma(ColorRGB c) {
	return static_cast<uint8_t>((c.r * 38 + c.g * 75 + c.b * 15) >> 7);
}

// ============================================================================
// Stage 1: ガウシアン / メディアン / Max / Min
// ============================================================================
static void ApplyStage1(LONG** src, LONG** dst, int stY, int edY, int width, int height, FilterStage1 type) {

	if (type == FilterStage1::NONE) {
		for (int y = stY; y < edY; y++) {
			LONG* srcLine = src[y];
			LONG* dstLine = dst[y];
			for (int x = 0; x < width + HOKAN_DOTS; x++) {
				dstLine[x] = srcLine[x];
			}
		}
		return;
	}

	if (type == FilterStage1::GAUSSIAN_3X3 || 
		type == FilterStage1::GAUSSIAN_5X5 || 
		type == FilterStage1::GAUSSIAN_7X7) {
		// ガウシアンフィルター
		int total_width = width + HOKAN_DOTS;
		for (int y = stY; y < edY; y++) {
			LONG* pDst = dst[y];
			for (int x = 0; x < total_width; x++) {
				int sumR = 0, sumG = 0, sumB = 0;
				// 3x3 処理
				if (type == FilterStage1::GAUSSIAN_3X3) {
					for (int dy = -1; dy <= 1; dy++) {
						for (int dx = -1; dx <= 1; dx++) {
							ColorRGB c = GetRGB(GetPixelSafe(src, x + dx, y + dy, width, height));
							int w = GAUSS_KERNEL_3x3[dy + 1][dx + 1];
							sumR += c.r * w;
							sumG += c.g * w;
							sumB += c.b * w;
						}
					}
					pDst[x] = MakeColorRGB(
						(uint8_t)(sumR >> 4),
						(uint8_t)(sumG >> 4),
						(uint8_t)(sumB >> 4)
					);
				}
				// 5x5 処理
				else if (type == FilterStage1::GAUSSIAN_5X5) {
					for (int dy = -2; dy <= 2; dy++) {
						for (int dx = -2; dx <= 2; dx++) {
						ColorRGB c = GetRGB(GetPixelSafe(src, x + dx, y + dy, width, height));
						int w = GAUSS_KERNEL_5x5[dy + 2][dx + 2];
						sumR += c.r * w;
						sumG += c.g * w;
						sumB += c.b * w;
					}
				}
					pDst[x] = MakeColorRGB(
						(uint8_t)std::clamp(sumR / 273, 0, 255),
						(uint8_t)std::clamp(sumG / 273, 0, 255),
						(uint8_t)std::clamp(sumB / 273, 0, 255)
					);
				}
				// 7x7 処理
				else if (type == FilterStage1::GAUSSIAN_7X7) {
					for (int dy = -3; dy <= 3; dy++) {
						for (int dx = -3; dx <= 3; dx++) {
							ColorRGB c = GetRGB(GetPixelSafe(src, x + dx, y + dy, width, height));
							int w = GAUSS_KERNEL_7x7[dy + 3][dx + 3];
							sumR += c.r * w;
							sumG += c.g * w;
							sumB += c.b * w;
						}
					}
					pDst[x] = MakeColorRGB(
						(uint8_t)std::clamp(sumR / 1003, 0, 255),
						(uint8_t)std::clamp(sumG / 1003, 0, 255),
						(uint8_t)std::clamp(sumB / 1003, 0, 255)
					);
				}
			}
		}
		return;
	}

	// メディアン / Max / Min フィルター
	int kSize = (type == FilterStage1::MEDIAN_5X5 || type == FilterStage1::MAX_5X5 || type == FilterStage1::MIN_5X5) ? 5 : (type == FilterStage1::MEDIAN_7X7) ? 7 : 3;
	const int r = kSize / 2;
	const int num_elements = kSize * kSize;
	const int median_idx = num_elements / 2;
	int total_width = width + HOKAN_DOTS;
	// パディング付き事前バッファ(1次元連続メモリ)を作成
	int padded_w = total_width + r * 2;
	int padded_h = height + r * 2;
	std::vector<ColorRGB> src_buf(padded_w * padded_h);
	#pragma omp parallel for schedule(static)
	for (int py = 0; py < padded_h; py++) {
		int srcY = std::clamp(py - r, 0, height - 1);
		for (int px = 0; px < padded_w; px++) {
			int x = px - r;
			src_buf[py * padded_w + px] = GetRGB(GetPixelSafe(src, x, srcY, width, height));
		}
	}
	// 画素と輝度をペアにして管理する構造体
	struct PixelLuma {
		ColorRGB color;
		uint8_t luma;
		bool operator<(const PixelLuma& other) const { return luma < other.luma; }
	};
	// -----------------------------------------
	// フィルターのタイプごとに処理を分岐
	// -----------------------------------------
	if (type == FilterStage1::MEDIAN_3X3 || type == FilterStage1::MEDIAN_5X5 || type == FilterStage1::MEDIAN_7X7) {
		// メディアンフィルター
		#pragma omp parallel for schedule(dynamic)
		for (int y = stY; y < edY; y++) {
			int py = y + r;
			for (int x = 0; x < total_width; x++) {
				int px = x + r;
				std::array<PixelLuma, 64> win; 
				int idx = 0;
				for (int dy = -r; dy <= r; dy++) {
					const ColorRGB* pSrc = &src_buf[(py + dy) * padded_w + (px - r)];
					for (int dx = -r; dx <= r; dx++) {
						ColorRGB c = *pSrc++;
						uint8_t luma = static_cast<uint8_t>((c.r * 38 + c.g * 75 + c.b * 15) >> 7);
						win[idx++] = { c, luma };
					}
				}
				// 有効範囲(num_elements分)だけで中央値を抽出
				std::nth_element(win.begin(), win.begin() + median_idx, win.begin() + num_elements);
				dst[y][x] = MakeColorRGB(win[median_idx].color.r, win[median_idx].color.g, win[median_idx].color.b);
			}
		}
	}
	else if (type == FilterStage1::MAX_3X3 || type == FilterStage1::MAX_5X5) {
		// Maxフィルター
		#pragma omp parallel for schedule(dynamic)
		for (int y = stY; y < edY; y++) {
			int py = y + r;
			for (int x = 0; x < total_width; x++) {
				int px = x + r;
				std::array<PixelLuma, 64> win;
				int idx = 0;
				for (int dy = -r; dy <= r; dy++) {
					const ColorRGB* pSrc = &src_buf[(py + dy) * padded_w + (px - r)];
					for (int dx = -r; dx <= r; dx++) {
						ColorRGB c = *pSrc++;
						uint8_t luma = static_cast<uint8_t>((c.r * 38 + c.g * 75 + c.b * 15) >> 7);
						win[idx++] = { c, luma };
					}
				}
				// 有効範囲(num_elements)内から最大値を取得
				auto max_it = std::max_element(win.begin(), win.begin() + num_elements);
				dst[y][x] = MakeColorRGB(max_it->color.r, max_it->color.g, max_it->color.b);
			}
		}
	}
	else if (type == FilterStage1::MIN_3X3 || type == FilterStage1::MIN_5X5) {
		// Minフィルター
		#pragma omp parallel for schedule(dynamic)
		for (int y = stY; y < edY; y++) {
			int py = y + r;
			for (int x = 0; x < total_width; x++) {
				int px = x + r;
				std::array<PixelLuma, 64> win;
				int idx = 0;
				for (int dy = -r; dy <= r; dy++) {
					const ColorRGB* pSrc = &src_buf[(py + dy) * padded_w + (px - r)];
					for (int dx = -r; dx <= r; dx++) {
						ColorRGB c = *pSrc++;
						uint8_t luma = static_cast<uint8_t>((c.r * 38 + c.g * 75 + c.b * 15) >> 7);
						win[idx++] = { c, luma };
					}
				}
				// 有効範囲(num_elements)内から最小値を取得
				auto min_it = std::min_element(win.begin(), win.begin() + num_elements);
				dst[y][x] = MakeColorRGB(min_it->color.r, min_it->color.g, min_it->color.b);
			}
		}
	}
}

// ============================================================================
// Stage 2: バイラテラル / ガイデッド / 異方性拡散 / NL-Means / ウェーブレット
// ============================================================================
static void ApplyStage2(LONG** src, LONG** dst, int stY, int edY, int width, int height, FilterStage2 type, const PipelineConfig& config) {

	if (type == FilterStage2::NONE) {
		for (int y = stY; y < edY; y++) {
			LONG* srcLine = src[y];
			LONG* dstLine = dst[y];
			for (int x = 0; x < width + HOKAN_DOTS; x++) {
				dstLine[x] = srcLine[x];
			}
		}
		return;
	}

	if (type == FilterStage2::BILATERAL) {
		// バイラテラルフィルター
		int total_width = width + HOKAN_DOTS;
		int r = config.radius;
		int kernel_size = 2 * r + 1;
		// 空間(距離)ガウス重みテーブルの作成
		std::vector<float> spatial_lut(kernel_size * kernel_size);
		float coeff_spatial = -0.5f / (config.sigma_spatial * config.sigma_spatial);
		for (int dy = -r; dy <= r; ++dy) {
			for (int dx = -r; dx <= r; ++dx) {
				float dist_sq = static_cast<float>(dx * dx + dy * dy);
				spatial_lut[(dy + r) * kernel_size + (dx + r)] = std::exp(dist_sq * coeff_spatial);
			}
		}
		// 範囲(輝度差 0～255)ガウス重みテーブルの作成
		float range_lut[256];
		float coeff_range = -0.5f / (config.sigma_range * config.sigma_range);
		for (int diff = 0; diff < 256; ++diff) {
			range_lut[diff] = std::exp((diff * diff) * coeff_range);
		}
		// パディング付き事前バッファの作成(最内ループのGetPixelSafeを消し去るための工夫)
		int padded_w = total_width + r * 2;
		int padded_h = height + r * 2;
		std::vector<uint8_t> lumaBuffer(padded_w * padded_h, 0);
		std::vector<ColorRGB> rgbBuffer(padded_w * padded_h);
		#pragma omp parallel for schedule(static)
		for (int py = 0; py < padded_h; py++) {
			int srcY = std::clamp(py - r, 0, height - 1);
			for (int px = 0; px < padded_w; px++) {
				int x = px - r;
				ColorRGB c = GetRGB(GetPixelSafe(src, x, srcY, width, height));
				// 輝度 Y の算出（共通重み用）
				uint8_t luma = static_cast<uint8_t>((c.r * 38 + c.g * 75 + c.b * 15) >> 7);
				int bufIdx = py * padded_w + px;
				lumaBuffer[bufIdx] = luma;
				rgbBuffer[bufIdx]  = c;
			}
		}
		// メイン処理(並列実行)
		#pragma omp parallel for schedule(dynamic)
		for (int y = stY; y < edY; y++) {
			int py = y + r;
			for (int x = 0; x < total_width; x++) {
				int px = x + r;
				int center_idx = py * padded_w + px;
				uint8_t centerLuma = lumaBuffer[center_idx];
				ColorRGB centerC = rgbBuffer[center_idx];
				float sum_w = 0.0f;
				float sum_pix_r = 0.0f, sum_pix_g = 0.0f, sum_pix_b = 0.0f;
				// 近傍ウィンドウのループ
				for (int dy = -r; dy <= r; ++dy) {
					int qy = py + dy;
					// 空間重み行と、近傍の輝度・RGB行へのポインタを直接取得
					const float* spatial_row = &spatial_lut[(dy + r) * kernel_size];
					const uint8_t* pLuma = &lumaBuffer[qy * padded_w + (px - r)];
					const ColorRGB* pRGB = &rgbBuffer[qy * padded_w + (px - r)];
					for (int dx = -r; dx <= r; ++dx) {
						// ポインタから直接データを取得(インクリメント)
						uint8_t neighborLuma = *pLuma++;
						ColorRGB neighborC = *pRGB++;
						float sp_w = spatial_row[dx + r];
						// 輝度差から共通の「色重み」を1つだけ計算
						int diffLuma = std::abs((int)centerLuma - (int)neighborLuma);
						float w = sp_w * range_lut[diffLuma];
						// 共通の重みで全チャンネルを蓄積
						sum_w += w;
						sum_pix_r += w * neighborC.r;
						sum_pix_g += w * neighborC.g;
						sum_pix_b += w * neighborC.b;
					}
				}
				// 最終出力
				if (sum_w > 0.0f) {
					dst[y][x] = MakeColorRGB(
						static_cast<uint8_t>(std::clamp(sum_pix_r / sum_w, 0.0f, 255.0f)),
						static_cast<uint8_t>(std::clamp(sum_pix_g / sum_w, 0.0f, 255.0f)),
						static_cast<uint8_t>(std::clamp(sum_pix_b / sum_w, 0.0f, 255.0f))
					);
				}
				else {
					dst[y][x] = MakeColorRGB(centerC.r, centerC.g, centerC.b);
				}
			}
		}
	}
	else if (type == FilterStage2::ANISOTROPIC_DIFFUSION) {
		// 異方性拡散フィルター
		// 領域ごとの逐次更新を維持しつつ、ポインタとインライン展開で高速化
		for (int y = stY; y < edY; y++) {
			// 行ごとのポインタを取得しておき、配列アクセスの計算コストを削減する
			LONG* pDstLine = dst[y];
			for (int x = 0; x < width + HOKAN_DOTS; x++) {
				// 初期値の取得
				ColorRGB centerC = GetRGB(GetPixelSafe(src, x, y, width, height));
				float curR = static_cast<float>(centerC.r);
				float curG = static_cast<float>(centerC.g);
				float curB = static_cast<float>(centerC.b);
				// イテレーション(逐次更新の挙動を維持)
				for (int iter = 0; iter < config.ad_iterations; iter++) {
					// 周囲の画素を取得
					ColorRGB cN = GetRGB(GetPixelSafe(src, x, y - 1, width, height));
					ColorRGB cS = GetRGB(GetPixelSafe(src, x, y + 1, width, height));
					ColorRGB cE = GetRGB(GetPixelSafe(src, x + 1, y, width, height));
					ColorRGB cW = GetRGB(GetPixelSafe(src, x - 1, y, width, height));
					// インデックス計算をまとめる
					int idxR = static_cast<int>(curR);
					int idxG = static_cast<int>(curG);
					int idxB = static_cast<int>(curB);

					float diffR = g_AD_DiffLUT[static_cast<int>(cN.r) - idxR + 255] + 
						  g_AD_DiffLUT[static_cast<int>(cS.r) - idxR + 255] +
						  g_AD_DiffLUT[static_cast<int>(cE.r) - idxR + 255] + 
						  g_AD_DiffLUT[static_cast<int>(cW.r) - idxR + 255];
					float diffG = g_AD_DiffLUT[static_cast<int>(cN.g) - idxG + 255] + 
						  g_AD_DiffLUT[static_cast<int>(cS.g) - idxG + 255] +
						  g_AD_DiffLUT[static_cast<int>(cE.g) - idxG + 255] + 
						  g_AD_DiffLUT[static_cast<int>(cW.g) - idxG + 255];
					float diffB = g_AD_DiffLUT[static_cast<int>(cN.b) - idxB + 255] + 
						  g_AD_DiffLUT[static_cast<int>(cS.b) - idxB + 255] +
						  g_AD_DiffLUT[static_cast<int>(cE.b) - idxB + 255] + 
						  g_AD_DiffLUT[static_cast<int>(cW.b) - idxB + 255];
					curR += diffR;
					curG += diffG;
					curB += diffB;
				}
				// 結果の書き込み
				pDstLine[x] = MakeColorRGB(
					static_cast<uint8_t>(std::clamp(curR, 0.0f, 255.0f)),
					static_cast<uint8_t>(std::clamp(curG, 0.0f, 255.0f)),
					static_cast<uint8_t>(std::clamp(curB, 0.0f, 255.0f))
				);
			}
		}
	}
	else if (type == FilterStage2::GUIDED) {
		// ガイデッドフィルター
		int r = config.guided_r;
		float eps = config.guided_eps * 255.0f * 255.0f;
		for (int y = stY; y < edY; y++) {
			for (int x = 0; x < width + HOKAN_DOTS; x++) {
				float sumR = 0, sumG = 0, sumB = 0;
				float sumR2 = 0, sumG2 = 0, sumB2 = 0;
				int count = 0;

				for (int dy = -r; dy <= r; dy++) {
					for (int dx = -r; dx <= r; dx++) {
						ColorRGB c = GetRGB(GetPixelSafe(src, x + dx, y + dy, width, height));
						sumR += c.r; sumR2 += c.r * c.r;
						sumG += c.g; sumG2 += c.g * c.g;
						sumB += c.b; sumB2 += c.b * c.b;
						count++;
					}
				}
				auto calcVal = [&](float sum, float sum2, uint8_t centerVal) -> uint8_t {
					float mean = sum / count;
					float var = (sum2 / count) - (mean * mean);
					float a = var / (var + eps);
					float b = mean * (1.0f - a);
					return (uint8_t)std::clamp(a * centerVal + b, 0.0f, 255.0f);
				};
				ColorRGB center = GetRGB(GetPixelSafe(src, x, y, width, height));
				dst[y][x] = MakeColorRGB(
					calcVal(sumR, sumR2, center.r),
					calcVal(sumG, sumG2, center.g),
					calcVal(sumB, sumB2, center.b)
				);
			}
		}
	}
	else if (type == FilterStage2::NL_MEANS) {
		// Non-Local Means(NL-means)フィルター
		// パラメータの定義(ここで pRad や sRad が定義される)
		int total_width = width + HOKAN_DOTS;
		int sRad = config.nlm_search_window / 2;
		int pRad = config.nlm_patch_size / 2;
		float h2 = config.nlm_h * config.nlm_h;
		// パケットサイズ・探索窓に基づいたパディング幅
		int pad = sRad + pRad;
		int padded_w = total_width + pad * 2;
		int padded_h = height + pad * 2;
		// 画像全体の輝度(Y)バッファ & RGBバッファをパディング付きで事前作成
		std::vector<uint8_t> lumaBuffer(padded_w * padded_h, 0);
		std::vector<uint32_t> rgbBuffer(padded_w * padded_h, 0);
		#pragma omp parallel for schedule(static)
		for (int py = 0; py < padded_h; py++) {
			int srcY = std::clamp(py - pad, 0, height - 1);
			for (int px = 0; px < padded_w; px++) {
				int x = px - pad;
				LONG pixel = GetPixelSafe(src, x, srcY, width, height);
				ColorRGB c = GetRGB(pixel);
				uint8_t luma = static_cast<uint8_t>((c.r * 38 + c.g * 75 + c.b * 15) >> 7);
				int bufIdx = py * padded_w + px;
				lumaBuffer[bufIdx] = luma;
				rgbBuffer[bufIdx]  = static_cast<uint32_t>(pixel);
			}
		}
		// pRadが確定した後にLUTを生成(サイズ上限を設けてキャッシュ効率を維持)
		int patch_pixels = (2 * pRad + 1) * (2 * pRad + 1);
		int max_dist_sq = patch_pixels * 255 * 255;
		int lut_size = std::min(max_dist_sq + 1, 65536); 
		std::vector<float> weight_lut(lut_size);
		for (int d = 0; d < lut_size; ++d) {
			weight_lut[d] = std::exp(-static_cast<float>(d) / h2);
		}
		// メイン処理(並列実行)
		#pragma omp parallel for schedule(dynamic)
		for (int y = stY; y < edY; y++) {
			int py = y + pad;
			LONG* pDst = dst[y];
			for (int x = 0; x < total_width; x++) {
				int px = x + pad;
				float sumW = 0.0f;
				float weightedR = 0.0f, weightedG = 0.0f, weightedB = 0.0f;
				for (int wy = -sRad; wy <= sRad; wy++) {
					int qy = py + wy;
					for (int wx = -sRad; wx <= sRad; wx++) {
						int qx = px + wx;
						int distSq = 0;
						for (int dy = -pRad; dy <= pRad; dy++) {
							const uint8_t* pCenter = &lumaBuffer[(py + dy) * padded_w + (px - pRad)];
							const uint8_t* pTarget = &lumaBuffer[(qy + dy) * padded_w + (qx - pRad)];
							int pLen = 2 * pRad + 1;
							for (int k = 0; k < pLen; k++) {
								int diff = static_cast<int>(pCenter[k]) - static_cast<int>(pTarget[k]);
								distSq += diff * diff;
							}
						}
						// 安全にクランプしてLUTを参照
						int lut_idx = (distSq < lut_size) ? distSq : (lut_size - 1);
						float weight = weight_lut[lut_idx];
						if (distSq >= max_dist_sq) {
							weight = 0.0f; 
						}
						if (weight > 0.0f) {
							uint32_t targetPixel = rgbBuffer[qy * padded_w + qx];
							ColorRGB tc = GetRGB(targetPixel);
							weightedR += weight * tc.r;
							weightedG += weight * tc.g;
							weightedB += weight * tc.b;
							sumW += weight;
						}
					}
				}
				if (sumW > 0.0f) {
					pDst[x] = MakeColorRGB(
						(uint8_t)std::clamp(weightedR / sumW, 0.0f, 255.0f),
						(uint8_t)std::clamp(weightedG / sumW, 0.0f, 255.0f),
						(uint8_t)std::clamp(weightedB / sumW, 0.0f, 255.0f)
					);
				}
				else {
					pDst[x] = GetPixelSafe(src, x, y, width, height);
				}
			}
		}
	}
	else if (type == FilterStage2::WAVELET_THRESHOLD) {
		// ウェーブレットフィルター
		uint16_t thresh = static_cast<uint16_t>(config.wavelet_threshold);
		int total_width = width + HOKAN_DOTS;
		// 共通のしきい値処理ラムダ式(ループの外に配置)
		auto processChannel = [thresh](uint8_t val0, uint8_t val1, uint8_t& out0, uint8_t& out1) {
			int low = ((int)val0 + (int)val1) / 2;
			int high = (int)val0 - (int)val1;
			int absHigh = std::abs(high);
			// ソフトしきい値処理
			int newHigh = (absHigh > thresh) ? (high > 0 ? absHigh - thresh : -(absHigh - thresh)) : 0;
			out0 = static_cast<uint8_t>(std::clamp(low + newHigh / 2, 0, 255));
			out1 = static_cast<uint8_t>(std::clamp(low - newHigh / 2, 0, 255));
		};
		for (int y = stY; y < edY; y++) {
			// 各ピクセルの処理結果を一時保持するバッファ
			std::vector<ColorRGB> pass1(total_width);
			std::vector<ColorRGB> pass2(total_width);
			// -----------------------------------------
			// Pass 1: 偶数ペア(x, x+1)で変換
			// -----------------------------------------
			for (int x = 0; x < total_width - 1; x += 2) {
				ColorRGB c0 = GetRGB(GetPixelSafe(src, x, y, width, height));
				ColorRGB c1 = GetRGB(GetPixelSafe(src, x + 1, y, width, height));
				processChannel(c0.r, c1.r, pass1[x].r, pass1[x+1].r);
				processChannel(c0.g, c1.g, pass1[x].g, pass1[x+1].g);
				processChannel(c0.b, c1.b, pass1[x].b, pass1[x+1].b);
			}
			if (total_width % 2 != 0) {
				pass1[total_width - 1] = GetRGB(GetPixelSafe(src, total_width - 1, y, width, height));
			}
			// -----------------------------------------
			// Pass 2: 奇数ペア(x-1, x)に相当する処理
			// Pass 1 の結果をベースにシフトして変換
			// -----------------------------------------
			// 左端は Pass 1 の結果を引き継ぐ
			pass2[0] = pass1[0];
			for (int x = 1; x < total_width - 1; x += 2) {
				// 元画像ではなく、Pass 1 の結果をペアにして処理する
				ColorRGB c0 = pass1[x];
				ColorRGB c1 = pass1[x + 1];
				processChannel(c0.r, c1.r, pass2[x].r, pass2[x+1].r);
				processChannel(c0.g, c1.g, pass2[x].g, pass2[x+1].g);
				processChannel(c0.b, c1.b, pass2[x].b, pass2[x+1].b);
			}
			if (total_width % 2 == 0) {
				pass2[total_width - 1] = pass1[total_width - 1];
			}
			// -----------------------------------------
			// Pass 1 と Pass 2 の結果を平均化して最終出力
			// -----------------------------------------
			for (int x = 0; x < total_width; x++) {
				uint8_t r = (static_cast<int>(pass1[x].r) + static_cast<int>(pass2[x].r)) / 2;
				uint8_t g = (static_cast<int>(pass1[x].g) + static_cast<int>(pass2[x].g)) / 2;
				uint8_t b = (static_cast<int>(pass1[x].b) + static_cast<int>(pass2[x].b)) / 2;
				dst[y][x] = MakeColorRGB(r, g, b);
			}
		}
	}
}

// ============================================================================
// Stage 3: モルフォロジー (Opening, Closing, Top-Hat, Black-Hat)
// ============================================================================
static void ApplyStage3(LONG** src, LONG** dst, int stY, int edY, int width, int height, FilterStage3 type) {

	if (type == FilterStage3::NONE) {
		for (int y = stY; y < edY; y++) {
			LONG* srcLine = src[y];
			LONG* dstLine = dst[y];
			for (int x = 0; x < width + HOKAN_DOTS; x++) {
				dstLine[x] = srcLine[x];
			}
		}
		return;
	}

	int total_width = width + HOKAN_DOTS;
	int num_rows = edY - stY;
	// 第1段階（Pass 1）の結果を保存する一時バッファ（行数分）
	std::vector<std::vector<ColorRGB>> pass1_buf(num_rows, std::vector<ColorRGB>(total_width));
	std::vector<std::vector<LONG>> tmpBuf(num_rows, std::vector<LONG>(total_width));
	// -----------------------------------------
	// STEP 1: 第1段階(Opening系は「収縮」、Closing系は「膨張」)
	// -----------------------------------------
	for (int y = stY; y < edY; y++) {
		int local_y = y - stY;
		for (int x = 0; x < total_width; x++) {
			std::array<PixelLuma, 9> win;
			int idx = 0;
			for (int dy = -1; dy <= 1; dy++) {
				for (int dx = -1; dx <= 1; dx++) {
					ColorRGB c = GetRGB(GetPixelSafe(src, x + dx, y + dy, width, height));
					win[idx++] = { c, CalcLuma(c) };
				}
			}
			// Opening / Top-Hat なら Min(収縮)、Closing / Black-Hat なら Max(膨張)
			if (type == FilterStage3::OPENING || type == FilterStage3::TOP_HAT) {
				pass1_buf[local_y][x] = std::min_element(win.begin(), win.end())->color;
			}
			else {
				pass1_buf[local_y][x] = std::max_element(win.begin(), win.end())->color;
			}
		}
	}
	// -----------------------------------------
	// STEP 2: 第2段階(Pass 1 の結果に対して逆の演算をかけ、最終的なモルフォロジーを生成)
	// -----------------------------------------
	// tmpBuf は元コードに合わせて std::vector<std::vector<LONG>> と仮定
	for (int y = stY; y < edY; y++) {
		int local_y = y - stY;
		for (int x = 0; x < total_width; x++) {
			std::array<PixelLuma, 9> win;
			int idx = 0;
			for (int dy = -1; dy <= 1; dy++) {
				// タイル分割境界を考慮した安全な行参照
				int target_y = y + dy;
				int target_local_y = target_y - stY;
				// スレッドの処理範囲外(stY未満 または edY以上)にアクセスする場合のフォールバック
				if (target_local_y < 0 || target_local_y >= num_rows) {
					// 範囲外は安全のため、クランプした安全な行を見るかエッジ複製処理にする
					int safe_y = std::clamp(target_y, stY, edY - 1);
					target_local_y = safe_y - stY;
				}
				for (int dx = -1; dx <= 1; dx++) {
					int target_x = std::clamp(x + dx, 0, total_width - 1);
					ColorRGB c = pass1_buf[target_local_y][target_x];
					win[idx++] = { c, CalcLuma(c) };
				}
			}
			ColorRGB morphColor;
			// 第1段階が収縮なら膨張、膨張なら収縮をかける
			if (type == FilterStage3::OPENING || type == FilterStage3::TOP_HAT) {
				morphColor = std::max_element(win.begin(), win.end())->color;
			}
			else {
				morphColor = std::min_element(win.begin(), win.end())->color;
			}
			// 後の処理で使いやすいようにLONG形式（MakeColorRGB）で保存
			tmpBuf[local_y][x] = MakeColorRGB(morphColor.r, morphColor.g, morphColor.b);
		}
	}
	// -----------------------------------------
	// STEP 3: 最終ブレンド ＆ コントラスト強調処理(Top-Hat / Black-Hat)
	// -----------------------------------------
	for (int y = stY; y < edY; y++) {
		int local_y = y - stY;
		LONG* pSrc  = src[y];
		LONG* pMorph = tmpBuf[local_y].data();
		LONG* pDst  = dst[y];
		if (type == FilterStage3::OPENING || type == FilterStage3::CLOSING) {
			std::memcpy(pDst, pMorph, sizeof(LONG) * total_width);
		}
		else if (type == FilterStage3::TOP_HAT) {
			for (int x = 0; x < total_width; x++) {
				ColorRGB cS = GetRGB(pSrc[x]);
				ColorRGB cM = GetRGB(pMorph[x]);
				uint8_t topHatR = (cS.r > cM.r) ? (cS.r - cM.r) : 0;
				uint8_t topHatG = (cS.g > cM.g) ? (cS.g - cM.g) : 0;
				uint8_t topHatB = (cS.b > cM.b) ? (cS.b - cM.b) : 0;
				pDst[x] = MakeColorRGB(
					(uint8_t)std::clamp((int)cS.r + (int)topHatR, 0, 255),
					(uint8_t)std::clamp((int)cS.g + (int)topHatG, 0, 255),
					(uint8_t)std::clamp((int)cS.b + (int)topHatB, 0, 255)
				);
			}
		} 
		else if (type == FilterStage3::BLACK_HAT) {
			for (int x = 0; x < total_width; x++) {
				ColorRGB cS = GetRGB(pSrc[x]);
				ColorRGB cM = GetRGB(pMorph[x]);
				uint8_t blackHatR = (cM.r > cS.r) ? (cM.r - cS.r) : 0;
				uint8_t blackHatG = (cM.g > cS.g) ? (cM.g - cS.g) : 0;
				uint8_t blackHatB = (cM.b > cS.b) ? (cM.b - cS.b) : 0;
				pDst[x] = MakeColorRGB(
					(uint8_t)std::clamp((int)cS.r - (int)blackHatR, 0, 255),
					(uint8_t)std::clamp((int)cS.g - (int)blackHatG, 0, 255),
					(uint8_t)std::clamp((int)cS.b - (int)blackHatB, 0, 255)
				);
			}
		}
	}
}

// ============================================================================
// Stage 4: S字カーブ / 適応的二値化 / 大津の二値化
// ============================================================================
static void ApplyStage4(LONG** src, LONG** dst, int stY, int edY, int width, int height, FilterStage4 type, const PipelineConfig& config) {

	if (type == FilterStage4::NONE) {
		for (int y = stY; y < edY; y++) {
			LONG* srcLine = src[y];
			LONG* dstLine = dst[y];
			for (int x = 0; x < width + HOKAN_DOTS; x++) {
				dstLine[x] = srcLine[x];
			}
		}
		return;
	}

	int total_width = width + HOKAN_DOTS;

	for (int y = stY; y < edY; y++) {
		LONG *srcLine = src[y];
		LONG *dstLine = dst[y];
		if (type == FilterStage4::S_CURVE) {
			// S字カーブフィルター
			int x = 0;
			// total_widthが4の倍数でなくても、完全に安全に4画素ずつNEON処理するガード
			for (; x <= total_width - 4; x += 4) {
				uint8x16_t vColor = vld1q_u8(reinterpret_cast<const uint8_t*>(&srcLine[x]));
				alignas(16) uint8_t bytes[16];
				vst1q_u8(bytes, vColor);
				for (int i = 0; i < 16; i += 4) {
					bytes[i + 0] = g_SCurveLUT[bytes[i + 0]];
					bytes[i + 1] = g_SCurveLUT[bytes[i + 1]];
					bytes[i + 2] = g_SCurveLUT[bytes[i + 2]];
				}
				vst1q_u8(reinterpret_cast<uint8_t*>(&dstLine[x]), vld1q_u8(bytes));
			}
			// 端数処理
			for (; x < total_width; x++) {
				ColorRGB c = GetRGB(GetPixelSafe(src, x, y, width, height));
				dstLine[x] = MakeColorRGB(g_SCurveLUT[c.r], g_SCurveLUT[c.g], g_SCurveLUT[c.b]);
			}
		}
		else if (type == FilterStage4::OTSU_THRESHOLD) {
			// 大津の二値化フィルター
			uint8_t th = config.otsu_threshold;
			for (int x = 0; x < total_width; x++) {
				ColorRGB c = GetRGB(GetPixelSafe(src, x, y, width, height));
				// 輝度(Y)を基準に判定
				uint8_t gray = static_cast<uint8_t>((c.r * 38 + c.g * 75 + c.b * 15) >> 7);
				uint8_t binVal = (gray > th) ? 255 : 0;

				// 完全な白黒(255, 255, 255)または(0, 0, 0)で出力
				dstLine[x] = MakeColorRGB(binVal, binVal, binVal);
			}
		}
		else if (type == FilterStage4::ADAPTIVE_THRESHOLD) {
			// 適応的二値化フィルター
			int r = config.adaptive_window_size / 2;
			for (int x = 0; x < total_width; x++) {
				ColorRGB c = GetRGB(GetPixelSafe(src, x, y, width, height));
				uint8_t centerLuma = static_cast<uint8_t>((c.r * 38 + c.g * 75 + c.b * 15) >> 7);

				int sumLuma = 0, count = 0;
				for (int dy = -r; dy <= r; dy++) {
					for (int dx = -r; dx <= r; dx++) {
						ColorRGB nc = GetRGB(GetPixelSafe(src, x + dx, y + dy, width, height));
						sumLuma += (nc.r * 38 + nc.g * 75 + nc.b * 15) >> 7;
						count++;
					}
				}
				uint8_t binVal = (centerLuma > (sumLuma / count - config.adaptive_c)) ? 255 : 0;
				dstLine[x] = MakeColorRGB(binVal, binVal, binVal);
			}
		}
	}
}

// ----------------------------------------------------------------------------
// フィルターの作業用のメモリーを確保
// ----------------------------------------------------------------------------
void InitWorkBuffers(int height, int width) {
	int pad = HOKAN_DOTS / 2;
	// 1行あたりに必要な全体のサイズ(本体幅 + 左右の補間用余白)
	int rowAllocSize = width + HOKAN_DOTS;
	// 第1層 高さ(行数)分のポインタ配列を確保
	gWorkLinesPtr = new LONG*[height];
	for (int y = 0; y < height; y++) {
		// 第2層 1行分の実際の画素データ領域を確保
		LONG* rawBuffer = new LONG[rowAllocSize];
		// 負のインデックスアクセス(buffptr[-2]等)への対応
		gWorkLinesPtr[y] = rawBuffer + pad;
	}
}

// ----------------------------------------------------------------------------
// フィルターの作業用のメモリーを開放
// ----------------------------------------------------------------------------
void FreeWorkBuffers(int height) {
	if (gWorkLinesPtr == nullptr) return;
	int pad = HOKAN_DOTS / 2;
	for (int y = 0; y < height; y++) {
		if (gWorkLinesPtr[y] != nullptr) {
			// オフセットした分を引き戻して元の先頭アドレスをdeleteする
			LONG* rawBuffer = gWorkLinesPtr[y] - pad;
			delete[] rawBuffer;
		}
	}
	// 第1層のポインタ配列を解放
	delete[] gWorkLinesPtr;
	gWorkLinesPtr = nullptr;
}

// ============================================================================
// マルチスレッド・エントリー関数
// ============================================================================

void *ImageFilterPipeline_ThreadFunc(void *param) {
	ThreadParam *p = (ThreadParam*)param;
	int stindex   = p->stindex;
	int edindex   = p->edindex;
	int OrgWidth  = p->OrgWidth;
	int OrgHeight = p->OrgHeight;
	int index	 = p->index;
	PipelineConfig config = p->config;

	if (gCancel[index]) {
		return (void*)ERROR_CODE_USER_CANCELED;
	}
	// 2つのバッファを用いて交互にフィルター処理を実行
	// 1回目のフィルター処理
	// gLinesPtr→gSclLinesPtr
	ApplyStage1(gLinesPtr[index], gSclLinesPtr[index], stindex, edindex, OrgWidth, OrgHeight, p->config.stage1);
	pthread_barrier_wait(p->pBarrier);
	// 直前の結果が入っているバッファ
	LONG** currentSrc = gSclLinesPtr[index];
	// 次の出力先バッファ
	LONG** currentDst = gWorkLinesPtr;
	// 2回目のフィルター処理
	// gSclLinesPtr→gWorkLinesPtr
	ApplyStage2(currentSrc, currentDst, stindex, edindex, OrgWidth, OrgHeight, p->config.stage2, p->config);
	pthread_barrier_wait(p->pBarrier);
	// バッファの順番を入れ替える
	std::swap(currentSrc, currentDst);
	// スワップ後: src=Work, dst=Scl
	// 3回目のフィルター処理
	// gWorkLinesPtr→gSclLinesPtr
	ApplyStage3(currentSrc, currentDst, stindex, edindex, OrgWidth, OrgHeight, p->config.stage3);
	pthread_barrier_wait(p->pBarrier);
	// バッファの順番を入れ替える
	std::swap(currentSrc, currentDst);
	// スワップ後: src=Scl, dst=Work
	// 4回目のフィルター処理
	// gSclLinesPtr→gWorkLinesPtr
	ApplyStage4(currentSrc, currentDst, stindex, edindex, OrgWidth, OrgHeight, p->config.stage4, p->config);
	pthread_barrier_wait(p->pBarrier);
	// gWorkLinesPtr→latestData
	LONG** latestData = currentDst;

	int pad = HOKAN_DOTS / 2;
	// latestData→gSclLinesPtr(右下のズレを補正して書き込む)
	for (int yy = stindex; yy < edindex; yy++) {
		LONG *buffptr = gSclLinesPtr[index][yy];
		// 4ステージ通過後の最新行(Y方向のズレをここで1回だけ補正して読み出す)
		// 縦方向のインデックスの範囲チェック(クランプ処理)
		int readY = yy + pad;
		if (readY >= OrgHeight) {
			// 画像の下端を超えたら最終行のデータで補う
			readY = OrgHeight - 1;
		}
		// ゴミ領域ではなくクランプされた安全な行を読み出す
		LONG *orgbuff = latestData[readY];
		// X方向のシフトを行ってgSclLinesPtrへ書き込む
		for (int xx = 0; xx < OrgWidth + HOKAN_DOTS; xx++) {
			buffptr[xx - pad] = orgbuff[xx];
		}
		// 補間用の端処理
		buffptr[-2] = buffptr[0];
		buffptr[-1] = buffptr[0];
		buffptr[OrgWidth + 0] = buffptr[OrgWidth - 1];
		buffptr[OrgWidth + 1] = buffptr[OrgWidth - 1];
	}

	return nullptr;
}

// ============================================================================
// メイン関数
// ============================================================================

int ImageFilterPipeline(int index, int Page, int Half, int Count, int OrgWidth, int OrgHeight, PipelineConfig config) {
	int ret = 0;
	int linesize = OrgWidth + HOKAN_DOTS;

	if (ScaleMemAlloc(index, linesize, OrgHeight) < 0) {
		return -6;
	}

	if (RefreshSclLinesPtr(index, Page, Half, Count, OrgHeight, linesize) < 0) {
		return -7;
	}
	// フィルターの作業用のメモリーを確保
	InitWorkBuffers(OrgHeight + HOKAN_DOTS, linesize);

	PrecomputeLUTs(config, gLinesPtr[index], OrgWidth, OrgHeight);

	pthread_barrier_t barrier;
	pthread_barrier_init(&barrier, nullptr, gMaxThreadNum);

	pthread_t thread[gMaxThreadNum];
	int start = 0;
	ThreadParam param[gMaxThreadNum];
	void *status[gMaxThreadNum];

	for (int i = 0; i < gMaxThreadNum; i++) {
		param[i].stindex   = start;
		param[i].edindex   = start = OrgHeight * (i + 1) / gMaxThreadNum;
		param[i].OrgWidth  = OrgWidth;
		param[i].OrgHeight = OrgHeight;
		param[i].index	 = index;
		param[i].config	= config;
		param[i].pBarrier  = &barrier;

		if (i < gMaxThreadNum - 1) {
			if (pthread_create(&thread[i], nullptr, ImageFilterPipeline_ThreadFunc, (void*)&param[i]) != 0) {
				LOGE("pthread_create()");
			}
		}
		else {
			status[i] = ImageFilterPipeline_ThreadFunc((void*)&param[i]);
		}
	}

	for (int i = 0; i < gMaxThreadNum; i++) {
		if (i < gMaxThreadNum - 1) {
			pthread_join(thread[i], &status[i]);
		}
		if (status[i] != 0) {
			ret = -10;
		}
	}

	pthread_barrier_destroy(&barrier);

	// フィルターの作業用のメモリーを開放
	FreeWorkBuffers(OrgHeight + HOKAN_DOTS);

	return ret;
}

int ImageExternalFilter(int index, int Page, int Half, int Count, int OrgWidth, int OrgHeight, jobject paramsObj, JNIEnv* env) {
	PipelineConfig config;
	// Java側のclassの塊がやってくる
	jclass clazz = env->GetObjectClass(paramsObj);
	// classの塊からパラメータを取り出す
	config.stage1 = static_cast<FilterStage1>(env->GetIntField(paramsObj, env->GetFieldID(clazz, "mFilterStage1", "I")));
	config.stage2 = static_cast<FilterStage2>(env->GetIntField(paramsObj, env->GetFieldID(clazz, "mFilterStage2", "I")));
	config.stage3 = static_cast<FilterStage3>(env->GetIntField(paramsObj, env->GetFieldID(clazz, "mFilterStage3", "I")));
	config.stage4 = static_cast<FilterStage4>(env->GetIntField(paramsObj, env->GetFieldID(clazz, "mFilterStage4", "I")));
	config.radius = env->GetIntField(paramsObj, env->GetFieldID(clazz, "mRadius", "I"));
	config.sigma_spatial = env->GetFloatField(paramsObj, env->GetFieldID(clazz, "mSigma_spatial", "F"));
	config.sigma_range = env->GetFloatField(paramsObj, env->GetFieldID(clazz, "mSigma_range", "F"));
	config.guided_r = env->GetIntField(paramsObj, env->GetFieldID(clazz, "mGuided_r", "I"));
	config.guided_eps = env->GetFloatField(paramsObj, env->GetFieldID(clazz, "mGuided_eps", "F"));
	config.ad_iterations = env->GetIntField(paramsObj, env->GetFieldID(clazz, "mAd_iterations", "I"));
	config.ad_k = env->GetFloatField(paramsObj, env->GetFieldID(clazz, "mAd_k", "F"));
	config.ad_lambda = env->GetFloatField(paramsObj, env->GetFieldID(clazz, "mAd_lambda", "F"));
	config.nlm_search_window = env->GetIntField(paramsObj, env->GetFieldID(clazz, "mNlm_search_window", "I"));
	config.nlm_patch_size = env->GetIntField(paramsObj, env->GetFieldID(clazz, "mNlm_patch_size", "I"));
	config.nlm_h = env->GetFloatField(paramsObj, env->GetFieldID(clazz, "mNlm_h", "F"));
	config.wavelet_threshold = (uint8_t)env->GetIntField(paramsObj, env->GetFieldID(clazz, "mWavelet_threshold", "I"));
	config.scurve_gain = env->GetFloatField(paramsObj, env->GetFieldID(clazz, "mScurve_gain", "F"));
	config.scurve_cutoff = env->GetFloatField(paramsObj, env->GetFieldID(clazz, "mScurve_cutoff", "F"));
	config.adaptive_window_size = env->GetIntField(paramsObj, env->GetFieldID(clazz, "mAdaptive_window_size", "I"));
	config.adaptive_c = env->GetIntField(paramsObj, env->GetFieldID(clazz, "mAdaptive_c", "I"));
	// メイン関数を呼び出す
	return ImageFilterPipeline(index, Page, Half, Count, OrgWidth, OrgHeight, config);
}

