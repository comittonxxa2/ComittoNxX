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

extern LONG			**gLinesPtr[];
extern LONG			**gSclLinesPtr[];
extern int			gCancel[];

extern int			gMaxThreadNum;

extern char gDitherX_3bit[8][8];
extern char gDitherX_2bit[4][4];
extern char gDitherY_3bit[8];
extern char gDitherY_2bit[4];

void *ImageBlur_ThreadFunc(void *param) {
	// ガウスぼかしへ変更、処理の最適化を行ってみた
	int *range = (int*)param;
	int stindex = range[0], edindex = range[1];
	int Width = range[2], Height = range[3], Zoom = range[4], index = range[5];
	// 作業用バッファ
	uint16_t tR[3][Width], tG[3][Width], tB[3][Width];
	// 重み合計が常に16(全体で256)になるように設計
	// w(端) * 2 + weight_c(中央) = 16
	int weight_c, w;
	if (Zoom < 100) {
		// 縮小時：標準 [4, 8, 4] 比率は1:2:1と同じ
		weight_c = 8;  w = 4;
	} else if (Zoom <= 120) {
		// 100-120%：最もモアレが出やすいため、縮小時と同じ強さにする
		weight_c = 8;  w = 4;
	} else if (Zoom <= 135) {
		// 120-135%：少し弱める [3, 10, 3]
		weight_c = 10; w = 3;
	} else if (Zoom <= 150) {
		// 135-150%：さらに弱める [2, 12, 2]
		weight_c = 12; w = 2;
	} else if (Zoom <= 170) {
		// 151-170%：かなり弱める [1, 14, 1]
		weight_c = 14; w = 1;
	} else {
		// 171%以上：ほぼぼかさない [0, 16, 0]
		weight_c = 16; w = 0;
	}
	// 横ぼかしをインライン化して効率化
	auto blur_h = [&](int row_y, int b_idx) {
		// 画像の上下端での範囲外アクセスを防ぐ
		int target_y = (row_y < 0) ? 0 : (row_y >= Height) ? Height - 1 : row_y;
		// 読み込みの基点を HOKAN_DOTS/2 に固定
		LONG *src = gLinesPtr[index][target_y + HOKAN_DOTS / 2];
		uint16_t *r = tR[b_idx], *g = tG[b_idx], *b = tB[b_idx];

		for (int x = 0; x < Width; x++) {
			// 水平方向の3タップフィルタ
			// 前後を参照するためx+1を中心にする
			// 右ズレを左に戻す
			int idxL = x + 1;
			int idxC = x + 2;
			int idxR = x + 3;
			// 右端でデータが途切れないための境界処理
			int limit = Width + (HOKAN_DOTS / 2) - 1;
			if (idxL > limit) idxL = limit;
			if (idxC > limit) idxC = limit;
			if (idxR > limit) idxR = limit;

			LONG pL = src[idxL], pC = src[idxC], pR = src[idxR];
			// 重み付け計算(合計16倍)
			r[x] = (uint16_t)(RGB888_RED(pL) * w + RGB888_RED(pC) * weight_c + RGB888_RED(pR) * w);
			g[x] = (uint16_t)(RGB888_GREEN(pL) * w + RGB888_GREEN(pC) * weight_c + RGB888_GREEN(pR) * w);
			b[x] = (uint16_t)(RGB888_BLUE(pL) * w + RGB888_BLUE(pC) * weight_c + RGB888_BLUE(pR) * w);
		}
	};
	// 初期行の充填
	// 0: 前の行 (prev), 1: 現在の行 (curr), 2: 次の行 (next)
	// 前の行 (stindex=0の時は0行目を複製)
	blur_h(stindex - 1, 0);
	// 現在の行
	blur_h(stindex, 1);
	// 縦ぼかしを実行
	for (int yy = stindex; yy < edindex; yy++) {
		if (gCancel[index]) break;
		// ローテーション用インデックスの計算
		// (yy - stindex)をベースにすることでスレッド境界での負数を回避
		// 垂直方向のシフト修正：時間軸に沿ったインデックス管理
		int p_idx = (yy - stindex + 0) % 3;
		int c_idx = (yy - stindex + 1) % 3;
		int n_idx = (yy - stindex + 2) % 3;
		// 次の行(yy + 1)を計算してバッファの空いている場所(n_idx)へ
		blur_h(yy + 1, n_idx);

		LONG *dst = gSclLinesPtr[index][yy];
		uint16_t *rP = tR[p_idx], *rC = tR[c_idx], *rN = tR[n_idx];
		uint16_t *gP = tG[p_idx], *gC = tG[c_idx], *gN = tG[n_idx];
		uint16_t *bP = tB[p_idx], *bC = tB[c_idx], *bN = tB[n_idx];

		for (int xx = 0; xx < Width; xx++) {
			// 加重平均(横で16倍、縦で16倍されているので256(>>8)で割る)
			// 中間計算のオーバーフローを防ぐためuint32_tで計算
			uint32_t red   = ((uint32_t)rP[xx] * w + (uint32_t)rC[xx] * weight_c + (uint32_t)rN[xx] * w) >> 8;
			uint32_t green = ((uint32_t)gP[xx] * w + (uint32_t)gC[xx] * weight_c + (uint32_t)gN[xx] * w) >> 8;
			uint32_t blue  = ((uint32_t)bP[xx] * w + (uint32_t)bC[xx] * weight_c + (uint32_t)bN[xx] * w) >> 8;

			dst[xx] = MAKE8888(red, green, blue);
		}
	}
	return 0;
}

// Margin     : 画像の何%まで余白チェックするか(0～20%)
// pOrgWidth  : 幅を指定
// pOrgHeight : 高さを指定
// Zoom       : 倍率（0%～100%→0～100で表す）
int ImageBlur(int index, int Page, int Half, int Count, int OrgWidth, int OrgHeight, int Zoom)
{
//	LOGD("ImageBlur : p=%d, h=%d, c=%d, ow=%d, oh=%d, zm=%d", Page, Half, Count, OrgWidth, OrgHeight, Zoom);

    int ret = 0;

    int		xx;	// サイズ変更後のx座標
    int		yy;	// サイズ変更後のy座標

    // 50%まで
    if (Zoom < 50) {
        Zoom = 50;
    }

    int linesize;

    // ラインサイズ
    linesize  = OrgWidth + HOKAN_DOTS;

    //  サイズ変更画像待避用領域確保
    if (ScaleMemAlloc(index, linesize, OrgHeight) < 0) {
        return -6;
    }

    // データの格納先ポインタリストを更新
    if (RefreshSclLinesPtr(index, Page, Half, Count, OrgHeight, linesize) < 0) {
        return -7;
    }

	pthread_t thread[gMaxThreadNum];
	int start = 0;
	int param[gMaxThreadNum][6];
	void *status[gMaxThreadNum];

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		param[i][0] = start;
		param[i][1] = start = OrgHeight * (i + 1)  / gMaxThreadNum;
		param[i][2] = OrgWidth;
		param[i][3] = OrgHeight;
		param[i][4] = Zoom;
        param[i][5] = index;

		if (i < gMaxThreadNum - 1) {
			/* スレッド起動 */
			if (pthread_create(&thread[i], nullptr, ImageBlur_ThreadFunc, (void*)param[i]) != 0) {
				LOGE("pthread_create()");
			}
		}
		else {
			// ループの最後は直接実行
			status[i] = ImageBlur_ThreadFunc((void*)param[i]);
		}
	}

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		/*thread_func()スレッドが終了するのを待機する。thread_func()スレッドが終了していたら、この関数はすぐに戻る*/
		if (i < gMaxThreadNum - 1) {
			pthread_join(thread[i], &status[i]);
		}
		if (status[i] != nullptr) {
//			LOGD("ImageBlur : cancel");
			ret = (long)status[i];
		}
	}

//	LOGD("ImageBlur : complete(%d)", ret);
	return ret;
}
