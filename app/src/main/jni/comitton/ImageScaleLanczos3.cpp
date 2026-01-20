#include <malloc.h>
#include <string.h>
#include <math.h>
#include <pthread.h>
#include <algorithm> // std::max, std::min用
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

extern int	*gSclIntParam1[];
extern int	*gSclIntParam2[];

// 円周率
#define PI 3.14159265358979323846f
// Lanczos3の窓の範囲
#define	N	3

// 重み情報を保持する構造体
typedef struct {
	// 参照開始インデックス
	int start;
	// 参照する画素数
	int count;     
	// スケールに応じた重み配列
	float *weights;
} WeightInfo;

// 画質優先のため中間データを保持する構造体
typedef struct {
	float r, g, b;
} FloatPixel;
// スレッド間共有データ
typedef struct {
	int stindex, edindex;
	int SclWidth, SclHeight;
	int OrgWidth, OrgHeight;
	int index;
	WeightInfo *hWeights;
	WeightInfo *vWeights;
	// 中間バッファ(水平リサイズ後)を高精度化
	FloatPixel **tempBuffer;
} ThreadParam;

// Lanczos3の窓関数
static float sinc(float x)
{
	if (x == 0) {
		return 1.0f;
	}
	float tx = x * PI;
	return sinf(tx) / tx;
}

// Lanczos3の窓関数を演算するかどうかを判定
static float lanczosWeight(float x)
{
	float absx = fabsf(x);
	if (absx == 0) {
		return 1.0f;
	}
	if (absx < N) {
		return sinc(absx) * sinc(absx / N);
	}
	return 0.0f;
}

// 重みテーブルの事前計算
void PrecomputeWeights(WeightInfo* table, int dstSize, int srcSize)
{
	float scale = (float)dstSize / srcSize;
	float rscale = 1.0f / scale;
	// 縮小時は窓を広げ、拡大時は窓を固定(1.0)にする
	float filterScale = (scale < 1.0f) ? scale : 1.0f;
	float support = (float)N / filterScale;
	// 各ピクセルの位置をサンプリングしてどの重みを掛けるべきかを事前に計算する
	for (int i = 0; i < dstSize; i++) {
		// 出力ピクセルの中心に対応する元画像の座標
		float center = (i + 0.5f) * rscale;
		// 影響範囲の開始と終了
		int start = (int)ceilf(center - support - 0.5f);
		int stop  = (int)floorf(center + support - 0.5f);
		table[i].start = start;
		table[i].count = stop - start + 1;
		table[i].weights = (float*)malloc(sizeof(float) * table[i].count);

		float sum = 0;
		for (int j = 0; j < table[i].count; j++) {
			float srcPos = (float)(start + j) + 0.5f;
			// Lanczos3の窓関数を演算して値をテーブルへ格納
			float weight = lanczosWeight((center - srcPos) * filterScale);
			table[i].weights[j] = weight;
			sum += weight;
		}
		// 正規化(明るさの変動を防ぐ)
		if (sum != 0) {
			for (int j = 0; j < table[i].count; j++) {
				// 平均を求める
				table[i].weights[j] /= sum;
			}
		}
	}
}

// 水平リサイズ実行
void *HorizontalPass_ThreadFunc(void *param)
{
	ThreadParam *p = (ThreadParam*)param;

	for (int y = p->stindex; y < p->edindex; y++) {
		if (gCancel[p->index]) {
			return (void*)ERROR_CODE_USER_CANCELED;
		}

		LONG *srcRow = gLinesPtr[p->index][y + HOKAN_DOTS / 2];
		FloatPixel *tmpRow = p->tempBuffer[y];

		for (int x = 0; x < p->SclWidth; x++) {
			WeightInfo &wi = p->hWeights[x];
			float rr = 0, gg = 0, bb = 0;

			for (int j = 0; j < wi.count; j++) {
				int srcX = std::max(0, std::min(p->OrgWidth - 1, wi.start + j));
				LONG pix = srcRow[srcX + HOKAN_DOTS / 2];
				// RGBデータを読み出して重み分を掛け算してから加算する
				rr += (float)RGB888_RED(pix) * wi.weights[j];
				gg += (float)RGB888_GREEN(pix) * wi.weights[j];
				bb += (float)RGB888_BLUE(pix) * wi.weights[j];
			}
			// 精度を維持して中間バッファへ書き込む
			tmpRow[x].r = rr;
			tmpRow[x].g = gg;
			tmpRow[x].b = bb;
		}
	}
	return nullptr;
}

// 垂直リサイズ実行
void *VerticalPass_ThreadFunc(void *param)
{
	ThreadParam *p = (ThreadParam*)param;

	for (int y = p->stindex; y < p->edindex; y++) {
		if (gCancel[p->index]) {
//			LOGD("CreateLanczos3 : cancel.");
//			ReleaseBuff(page, 1, half);
			return (void*)ERROR_CODE_USER_CANCELED;
		}

		LONG *dstRow = gSclLinesPtr[p->index][y];
		WeightInfo &wi = p->vWeights[y];

		for (int x = 0; x < p->SclWidth; x++) {
			float rr = 0, gg = 0, bb = 0;

			for (int j = 0; j < wi.count; j++) {
				int srcY = std::max(0, std::min(p->OrgHeight - 1, wi.start + j));
				FloatPixel &fp = p->tempBuffer[srcY][x];
				// RGBデータを読み出して重み分を掛け算してから加算する
				rr += fp.r * wi.weights[j];
				gg += fp.g * wi.weights[j];
				bb += fp.b * wi.weights[j];
			}
			// 0～255に収める(+0.5fを加えて四捨五入することで画質を改善)
			// バッファへRGBデータを書き込む
			dstRow[x] = MAKE8888((int)LIMIT_RGB(rr + 0.5f), (int)LIMIT_RGB(gg + 0.5f), (int)LIMIT_RGB(bb + 0.5f));
		}
		// 補完用の余裕
		dstRow[-2] = dstRow[0];
		dstRow[-1] = dstRow[0];
		dstRow[p->SclWidth + 0] = dstRow[p->SclWidth - 1];
		dstRow[p->SclWidth + 1] = dstRow[p->SclWidth - 1];
	}
	return nullptr;
}

int CreateScaleLanczos3(int index, int Page, int Half, int Count, int SclWidth, int SclHeight, int OrgWidth, int OrgHeight)
{
	int linesize;
	int ret = 0;

	linesize  = SclWidth + HOKAN_DOTS;

	//  サイズ変更演算領域用領域確保
    ret = ScaleMemColumn(index, SclWidth);
	if (ret < 0) {
		return ret;
	}

	//  サイズ変更画像待避用領域確保
    ret = ScaleMemAlloc(index, linesize, SclHeight);
	if (ret < 0) {
		return ret;
	}

	// データの格納先ポインタリストを更新
    ret = RefreshSclLinesPtr(index, Page, Half, Count, SclHeight, linesize);
	if (ret < 0) {
		return ret;
	}

	// 処理を軽くするため、重みテーブルを事前に計算を行い、水平と垂直のリサイズ処理を別々にした

	// 重みテーブル事前計算
	WeightInfo *hWeights = (WeightInfo*)malloc(sizeof(WeightInfo) * SclWidth);
	WeightInfo *vWeights = (WeightInfo*)malloc(sizeof(WeightInfo) * SclHeight);
	PrecomputeWeights(hWeights, SclWidth, OrgWidth);
	PrecomputeWeights(vWeights, SclHeight, OrgHeight);

	// 中間バッファ領域確保(画質維持のためFloatPixelを使用)
	FloatPixel **tempBuffer = (FloatPixel**)malloc(sizeof(FloatPixel*) * OrgHeight);
	for (int i = 0; i < OrgHeight; i++) {
		tempBuffer[i] = (FloatPixel*)malloc(sizeof(FloatPixel) * SclWidth);
	}

	pthread_t thread[gMaxThreadNum];
	ThreadParam params[gMaxThreadNum];
	void *status[gMaxThreadNum];
	// スレッドを水平と垂直で別々に実行
	// 水平リサイズ実行
	int start = 0;
	for (int i = 0; i < gMaxThreadNum; i++) {
		/*HorizontalPass_ThreadFuncスレッドが終了するのを待機する。HorizontalPass_ThreadFunc()スレッドが終了していたら、この関数はすぐに戻る*/
		params[i].stindex = start;
		params[i].edindex = start = OrgHeight * (i + 1) / gMaxThreadNum;
		params[i].SclWidth = SclWidth;
		params[i].SclHeight = SclHeight;
		params[i].OrgWidth = OrgWidth;
		params[i].OrgHeight = OrgHeight;
		params[i].index = index;
		params[i].hWeights = hWeights;
		params[i].vWeights = vWeights;
		params[i].tempBuffer = tempBuffer;

		if (i < gMaxThreadNum - 1) {
			/* スレッド起動 */
			pthread_create(&thread[i], nullptr, HorizontalPass_ThreadFunc, &params[i]);
		}
		else {
			// ループの最後は直接実行
			status[i] = HorizontalPass_ThreadFunc(&params[i]);
		}
	}
	for (int i = 0; i < gMaxThreadNum - 1; i++) {
		pthread_join(thread[i], &status[i]);
	}

	// 垂直リサイズ実行
	start = 0;
	for (int i = 0; i < gMaxThreadNum; i++) {
		/*VerticalPass_ThreadFunc()スレッドが終了するのを待機する。VerticalPass_ThreadFunc()スレッドが終了していたら、この関数はすぐに戻る*/
		params[i].stindex = start;
		params[i].edindex = start = SclHeight * (i + 1) / gMaxThreadNum;

		if (i < gMaxThreadNum - 1) {
			/* スレッド起動 */
			pthread_create(&thread[i], nullptr, VerticalPass_ThreadFunc, &params[i]);
		}
		else {
			// ループの最後は直接実行
			status[i] = VerticalPass_ThreadFunc(&params[i]);
		}
	}
	for (int i = 0; i < gMaxThreadNum - 1; i++) {
		pthread_join(thread[i], &status[i]);
	}

	// リソース解放
	for (int i = 0; i < SclWidth; i++) {
		free(hWeights[i].weights);
	}
	for (int i = 0; i < SclHeight; i++) {
		free(vWeights[i].weights);
	}
	for (int i = 0; i < OrgHeight; i++) {
		free(tempBuffer[i]);
	}
	free(tempBuffer);
	free(hWeights);
	free(vWeights);

	for (int i = 0; i < gMaxThreadNum; i++) {
		if (status[i] != nullptr) ret = (long)status[i];
	}

	return ret;
}
