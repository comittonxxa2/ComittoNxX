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

// 小数点以下第二桁までのLanczos3の窓関数の演算結果を保存するテーブルを確保する
static	float tabledata[1000];

// Lanczos3の窓関数
static float sinc(float x)
{
	return sin(x * PI) / (x * PI); 
}

// Lanczos3の窓関数を演算するかどうかを判定
static float lanczosWeight(float x, float n)
{
	return x == 0 ? 1 : (abs(x) < n ? sinc(x) * sinc(x / n) : 0);
}

void *CreateScaleLanczos3_ThreadFunc(void *param)
{
	int *range = (int*)param;
	int stindex   = range[0];
	int edindex   = range[1];
	int SclWidth  = range[2];
	int SclHeight = range[3];
	int OrgWidth  = range[4];
	int OrgHeight = range[5];
    int index = range[6];

//	LOGD("CreateScaleLanczos3_ThreadFund : st=%d, ed=%d, sw=%d, sh=%d, ow=%d, oh=%d", stindex, edindex, SclWidth, SclHeight, OrgWidth, OrgHeight);

	int xx;	// サイズ変更後のx座標
	int yy;	// サイズ変更後のy座標

    LONG *buffptr = nullptr;
    LONG *orgbuff1;

	// 元座標での最大値
	// 画面の端まで処理させるため切り上げ処理にする
	int bymax = ceil((float)edindex * (float)OrgHeight / (float)SclHeight);
	int bxmax = ceil((float)SclWidth * (float)OrgWidth / (float)SclWidth);

	// サイズ変更後の比率(一先ず横座標から演算する)
	float scale = (float)SclWidth / (float)OrgWidth;
	// 拡大の場合は比率が1を超えないようにする
	scale = (scale > 1) ? 1 : scale;

	// 元座標の横幅の比率(サイズ変更前なので逆になる)
	float rwidth = (float)OrgWidth / (float)SclWidth;
	// 元座標の高さの比率(サイズ変更前なので逆になる)
	float rheight = (float)OrgHeight / (float)SclHeight;

	// 縮小の場合は参照の範囲を増やす
	int scans = ceil(-N / scale);
	int scane = ceil(N / scale);

	// サイズ変更後のy座標
	for (yy = stindex ; yy < edindex ; yy ++) {

		if (gCancel[index]) {
//			LOGD("CreateLanczos3 : cancel.");
//			ReleaseBuff(page, 1, half);
			return (void*)ERROR_CODE_USER_CANCELED;
		}

		// バッファ位置
		buffptr = gSclLinesPtr[index][yy];

		// サイズ変更後のx座標
		for (xx = 0 ; xx < SclWidth ; xx++) {

		 	float rr = .0, gg = .0, bb = .0;
		 	float sum = .0;
		 	float lancdata,lancx,lancy,fwidth,fheight;
		 	int dx,dy;
		 	int xn,yn;
		 	float absx,absy;
			// 元座標に横幅/高さの比率を掛ける(座標の基準はピクセルの中心なので0.5を加算)
			fwidth = xx * rwidth + 0.5;
			fheight = yy * rheight + 0.5;
			// 元座標の整数のみ
			xn = fwidth;
			yn = fheight;
			// 元座標の整数分からのオフセットと元座標との距離を参照する
			for (int offsety = yn + scans ; offsety <= yn + scane ; offsety++) {
				// 元座標の整数分からのオフセットと元座標との距離を計算(座標の基準はピクセルの中心なのでオフセットに0.5を加算してサイズ変更後の比率を掛ける)
				absy = abs(fheight - (offsety + 0.5)) * scale;
				// 距離を小数点以下第二桁に丸め込む(0～9.99)
				int shifty = round(absy * 100);
				// あらかじめ演算しておいた重み分を取り出す
				lancy = tabledata[shifty];
				// 元座標の整数分からのオフセットと元座標との距離を参照する
				for (int offsetx = xn + scans ; offsetx <= xn + scane ; offsetx++) {
					// 元座標の整数分からのオフセットと元座標との距離を計算(座標の基準はピクセルの中心なのでオフセットに0.5を加算してサイズ変更後の比率を掛ける)
					absx = abs(fwidth - (offsetx + 0.5)) * scale;
					// 距離を小数点以下第二桁に丸め込む(0～9.99)
					int shiftx = round(absx * 100);
					// あらかじめ演算しておいた重み分を取り出す
					lancx = tabledata[shiftx];
					// 縦と横の重み分を掛け算
					lancdata = lancx * lancy;
					// 合計を計算
					sum += lancdata;
					// 画面外にはみ出ないかどうかを調べてリミッタを掛ける
					dy = offsety;
					dy = (dy < 0) ? 0 : (dy > bymax) ? bymax : dy;
					dx = offsetx;
					dx = (dx < 0) ? 0 : (dx > bxmax) ? bxmax : dx;
					orgbuff1 = gLinesPtr[index][dy + HOKAN_DOTS / 2];
					dx += HOKAN_DOTS / 2;
					// RGBデータを読み出して重み分を掛け算してから加算する
					rr += ((float)RGB888_RED(orgbuff1[dx])) * lancdata;
					gg += ((float)RGB888_GREEN(orgbuff1[dx])) * lancdata;
					bb += ((float)RGB888_BLUE(orgbuff1[dx])) * lancdata;
				}
			}
			// 平均を求める
			if (sum != 0) {
				rr /= sum;
				gg /= sum;
				bb /= sum;
			}
			// 0～255に収める
			rr = LIMIT_RGB((int)rr);
			gg = LIMIT_RGB((int)gg);
			bb = LIMIT_RGB((int)bb);
			// バッファへRGBデータを書き込む
			buffptr[xx] = MAKE8888((int)rr, (int)gg, (int)bb);
		}
		// 補完用の余裕
		buffptr[-2] = buffptr[0];
		buffptr[-1] = buffptr[0];
		buffptr[SclWidth + 0] = buffptr[SclWidth - 1];
		buffptr[SclWidth + 1] = buffptr[SclWidth - 1];
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

	// あらかじめLanczos3の窓関数の演算を小数点以下第二桁まで行う(※実際には窓関数の範囲外の3.00以上でリミッタが掛かるので実質300までになるがそのまま演算する)
	for (int i = 0 ; i < 1000; i++) {
		tabledata[i] = lanczosWeight((float)i / 100, N);
	}

	pthread_t thread[gMaxThreadNum];
	int start = 0;
	int param[gMaxThreadNum][7];
	void *status[gMaxThreadNum];

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		param[i][0] = start;
		param[i][1] = start = SclHeight * (i + 1)  / gMaxThreadNum;
		param[i][2] = SclWidth;
		param[i][3] = SclHeight;
		param[i][4] = OrgWidth;
		param[i][5] = OrgHeight;
        param[i][6] = index;

		if (i < gMaxThreadNum - 1) {
			/* スレッド起動 */
			if (pthread_create(&thread[i], nullptr, CreateScaleLanczos3_ThreadFunc, (void*)param[i]) != 0) {
				LOGE("pthread_create()");
			}
		}
		else {
			// ループの最後は直接実行
			status[i] = CreateScaleLanczos3_ThreadFunc((void*)param[i]);
		}
	}

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		/*thread_func()スレッドが終了するのを待機する。thread_func()スレッドが終了していたら、この関数はすぐに戻る*/
		if (i < gMaxThreadNum - 1) {
			pthread_join(thread[i], &status[i]);
		}
		if (status[i] != nullptr) {
			ret = (long)status[i];
		}
	}

	return ret;
}
