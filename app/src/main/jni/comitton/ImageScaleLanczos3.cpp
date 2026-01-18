#include <malloc.h>
#include <string.h>
#include <math.h>
#include <pthread.h>
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
#define PI	3.1415926
// Lanczos3の窓の範囲
#define	N	3

// 重み情報を保持する構造体
typedef struct {
	// 参照開始インデックス
	int start;
	// Lanczos3(N=3)の場合は最大6つの重み
	float weights[6];
} WeightInfo;

// スレッド間共有データ
typedef struct {
	int stindex, edindex;
	int SclWidth, SclHeight;
	int OrgWidth, OrgHeight;
	int index;
	WeightInfo *hWeights;
	WeightInfo *vWeights;
	// 中間バッファ(水平リサイズ後)
	LONG **tempBuffer;
} ThreadParam;

// Lanczos3の窓関数
static float sinc(float x)
{
	if (x == 0) {
		return 1.0f;
	}
	x *= PI;
	return sinf(x) / x;
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
void PrecomputeWeights(WeightInfo* table, int dstSize, int srcSize, float scale)
{
	float rscale = 1.0f / scale;
	float filterScale = (scale < 1.0f) ? scale : 1.0f;
	float support = (float)N / filterScale;
	// 各ピクセルの位置をサンプリングしてどの重みを掛けるべきかを事前に計算する
	for (int i = 0; i < dstSize; i++) {
		float center = (i + 0.5f) * rscale;
		int start = (int)floorf(center - support);
		table[i].start = start;
		float sum = 0;
		for (int j = 0; j < 6; j++) {
			int srcPos = start + j;
			// Lanczos3の窓関数を演算して値をテーブルへ格納
			float w = lanczosWeight((center - (srcPos + 0.5f)) * filterScale);
			table[i].weights[j] = w;
			sum += w;
		}
		if (sum != 0) {
			for (int j = 0; j < 6; j++) {
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
		LONG *tmpRow = p->tempBuffer[y];

		for (int x = 0; x < p->SclWidth; x++) {
			WeightInfo &wi = p->hWeights[x];
			float rr = 0, gg = 0, bb = 0;

			for (int j = 0; j < 6; j++) {
				int srcX = wi.start + j;
				srcX = (srcX < 0) ? 0 : (srcX >= p->OrgWidth) ? p->OrgWidth - 1 : srcX;
				LONG pix = srcRow[srcX + HOKAN_DOTS / 2];
				// RGBデータを読み出して重み分を掛け算してから加算する
				rr += (float)RGB888_RED(pix) * wi.weights[j];
				gg += (float)RGB888_GREEN(pix) * wi.weights[j];
				bb += (float)RGB888_BLUE(pix) * wi.weights[j];
			}
			// 0～255に収める
			// バッファへRGBデータを書き込む
			tmpRow[x] = MAKE8888((int)LIMIT_RGB(rr), (int)LIMIT_RGB(gg), (int)LIMIT_RGB(bb));
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

			for (int j = 0; j < 6; j++) {
				int srcY = wi.start + j;
				srcY = (srcY < 0) ? 0 : (srcY >= p->OrgHeight) ? p->OrgHeight - 1 : srcY;
				LONG pix = p->tempBuffer[srcY][x];
				// RGBデータを読み出して重み分を掛け算してから加算する
				rr += (float)RGB888_RED(pix) * wi.weights[j];
				gg += (float)RGB888_GREEN(pix) * wi.weights[j];
				bb += (float)RGB888_BLUE(pix) * wi.weights[j];
			}
			// 0～255に収める
			// バッファへRGBデータを書き込む
			dstRow[x] = MAKE8888((int)LIMIT_RGB(rr), (int)LIMIT_RGB(gg), (int)LIMIT_RGB(bb));
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
	PrecomputeWeights(hWeights, SclWidth, OrgWidth, (float)SclWidth / OrgWidth);
	PrecomputeWeights(vWeights, SclHeight, OrgHeight, (float)SclHeight / OrgHeight);

	// 中間バッファ領域確保
	LONG **tempBuffer = (LONG**)malloc(sizeof(LONG*) * OrgHeight);
	for (int i = 0; i < OrgHeight; i++) {
		tempBuffer[i] = (LONG*)malloc(sizeof(LONG) * SclWidth);
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
