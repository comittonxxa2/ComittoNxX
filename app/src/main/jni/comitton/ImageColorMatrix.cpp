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

float filter[20];

void *ImageColorMatrix_ThreadFunc(void *param)
{
	int *range = (int*)param;
	int stindex   = range[0];
	int edindex   = range[1];
	int OrgWidth  = range[2];
	int OrgHeight = range[3];
    int index = range[4];

    LONG *buffptr = nullptr;

    // �g�p����o�b�t�@��ێ�
    LONG *orgbuff;

	int		xx;	// x���W
	int		yy;	// y���W

	int rr, gg, bb;
	float rf, gf, bf;

	// ���C����
	for (yy = stindex ; yy < edindex ; yy ++) {
//		LOGD("ImageGray : loop yy=%d", yy);
		if (gCancel[index]) {
			LOGD("ImageGray : cancel.");
//			ReleaseBuff(Page, 1, Half);
			return (void*)ERROR_CODE_USER_CANCELED;
		}

        // �o�b�t�@�ʒu
        buffptr = gSclLinesPtr[index][yy];

        orgbuff = gLinesPtr[index][yy + HOKAN_DOTS / 2];

		for (xx =  0 ; xx < OrgWidth + HOKAN_DOTS ; xx++) {
			// �J���[�}�g���b�N�X�����Z
			rf = (float)RGB888_RED(orgbuff[xx]);
			gf = (float)RGB888_GREEN(orgbuff[xx]);
			bf = (float)RGB888_BLUE(orgbuff[xx]);
			rr = filter[0] * rf + filter[1] * gf + filter[2] * bf + filter[4];
			if (rr < 0) rr = 0;
			if (rr > 255) rr = 255;
			gg = filter[5] * rf + filter[6] * gf + filter[7] * bf + filter[9];
			if (gg < 0) gg = 0;
			if (gg > 255) gg = 255;
			bb = filter[10] * rf + filter[11] * gf + filter[12] * bf + filter[14];
			if (bb < 0) bb = 0;
			if (bb > 255) bb = 255;

            buffptr[xx - HOKAN_DOTS / 2] = MAKE8888(rr, gg, bb);
		}

		// �⊮�p�̗]�T
        buffptr[-2] = buffptr[0];
        buffptr[-1] = buffptr[0];
        buffptr[OrgWidth + 0] = buffptr[OrgWidth - 1];
        buffptr[OrgWidth + 1] = buffptr[OrgWidth - 1];
	}
	return nullptr;
}

// �J���[�}�g���b�N�X
int ImageColorMatrix(int index, int Page, int Half, int Count, int OrgWidth, int OrgHeight, jfloat *colormatrix)
{
//	LOGD("ImageGray : p=%d, h=%d, c=%d, ow=%d, oh=%d", Page, Half, Count, OrgWidth, OrgHeight);

	int ret = 0;

	int linesize;
	// �J���[�}�g���b�N�X�̗v�f��ۑ�
	memcpy(filter, colormatrix, sizeof(filter));

	// ���C���T�C�Y
	linesize  = OrgWidth + HOKAN_DOTS;

	//  �T�C�Y�ύX�摜�Ҕ�p�̈�m��
	if (ScaleMemAlloc(index, linesize, OrgHeight) < 0) {
		return -6;
	}

	// �f�[�^�̊i�[��|�C���^���X�g���X�V
	if (RefreshSclLinesPtr(index, Page, Half, Count, OrgHeight, linesize) < 0) {
		return -7;
	}

	pthread_t thread[gMaxThreadNum];
	int start = 0;
	int param[gMaxThreadNum][5];
	void *status[gMaxThreadNum];

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		param[i][0] = start;
		param[i][1] = start = OrgHeight * (i + 1)  / gMaxThreadNum;
		param[i][2] = OrgWidth;
		param[i][3] = OrgHeight;
        param[i][4] = index;

		if (i < gMaxThreadNum - 1) {
			/* �X���b�h�N�� */
			if (pthread_create(&thread[i], nullptr, ImageColorMatrix_ThreadFunc, (void*)param[i]) != 0) {
				LOGE("pthread_create()");
			}
		}
		else {
			// ���[�v�̍Ō�͒��ڎ��s
			status[i] = ImageColorMatrix_ThreadFunc((void*)param[i]);
		}
	}

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		/*thread_func()�X���b�h���I������̂�ҋ@����Bthread_func()�X���b�h���I�����Ă�����A���̊֐��͂����ɖ߂�*/
		if (i < gMaxThreadNum - 1) {
			pthread_join(thread[i], &status[i]);
		}
		if (status[i] != nullptr) {
//			LOGD("CreateScaleCubic : cancel");
			ret = (long)status[i];
		}
	}
//	LOGD("ImageGray : complete");
	return ret;
}