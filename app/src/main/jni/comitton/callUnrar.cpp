#include <jni.h>
#include <time.h>
#include <malloc.h>
#include <string.h>
#include <android/log.h>
#include <stdio.h>
#include <memory>
#include <unrar/rar.hpp>
#include "common.h"

#define ERROR_CODE_MALLOC_FAILURE -1001

std::unique_ptr<byte[]> ToBuff = nullptr;
std::unique_ptr<byte[]> FromBuff = nullptr;
int		FromPos = 0;
int		ToPos = 0;

int		MaxCompLen = 0;
int		MaxOrigLen = 0;
int		CompLen = 0;
int		OrigLen = 0;
int		RarVersion = 0;
bool	NoCompress = false;

ComprDataIO	DataIO;
std::unique_ptr<Unpack> Unp = nullptr;

extern "C" {
/*
 * Class:     src_comitton_stream_callLibrary
 * Method:    rarAlloc
 * Signature: (I[II)V
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallJniLibrary_rarAlloc(JNIEnv *env, jclass obj, jint cmplen, jint orglen)
{
	std::unique_ptr<byte[]> newFromBuff;
	std::unique_ptr<byte[]> newToBuff;

	try {
		newFromBuff = std::make_unique<byte[]>(cmplen);
		newToBuff = std::make_unique<byte[]>(orglen);
	}
	catch (const std::bad_alloc&) {
		return ERROR_CODE_MALLOC_FAILURE;
	}

	if (newFromBuff == nullptr || newToBuff == nullptr) {
		return ERROR_CODE_MALLOC_FAILURE;
	}

	FromBuff = std::move(newFromBuff);
	ToBuff = std::move(newToBuff);

	MaxCompLen = cmplen;
	MaxOrigLen = orglen;

	if (Unp == nullptr) {
		Unp = std::make_unique<Unpack>(&DataIO);
	}
	
	Unp->Init(UNPACK_MAX_WRITE, false);
	return 0;
}

/*
 * Class:     src_comitton_stream_callLibrary
 * Method:    rarInit
 * Signature: (I[II)V
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallJniLibrary_rarInit(JNIEnv *env, jclass obj, jint cmplen, jint orglen, jint rarver, jboolean nocomp)
{
#ifdef DEBUG
	LOGD("rarInit : cl=%d, ol=%d, rv=%d, nc=%d", cmplen, orglen, rarver, (int)nocomp);
#endif
	if (FromBuff == nullptr || ToBuff == nullptr) {
		return -1;
	}
	if (MaxCompLen < cmplen || MaxOrigLen < orglen) {
		return -2;
	}

	CompLen = cmplen;
	OrigLen = orglen;
	RarVersion = rarver;
	NoCompress = nocomp;

	FromPos = 0;
	ToPos = 0;

//	memset(FromBuff, 0, cmplen);
//	memset(ToBuff, 0, orglen);
	return 0;
}

/*
 * Class:     src_comitton_stream_callLibrary
 * Method:    rarWrite
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallJniLibrary_rarWrite(JNIEnv *env, jclass obj, jbyteArray cmpArray, jint offset, jint size)
{
	if (FromBuff == nullptr) {
		return -1;
	}

	jbyte *data = env->GetByteArrayElements(cmpArray, NULL);

	if (CompLen - FromPos < size) {
		// バッファサイズまでしか書き込まない
		size = CompLen - FromPos;
	}

	memcpy(&FromBuff[FromPos], &data[offset], size);
	FromPos += size;

	env->ReleaseByteArrayElements(cmpArray, data, 0);
	return size;
}

/*
 * Class:     src_comitton_stream_callLibrary
 * Method:    rarDecomp
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallJniLibrary_rarDecomp(JNIEnv *env, jclass obj)
{
#ifdef DEBUG
	LOGD("rarDecomp : NoCompress=%d", (int)NoCompress);
#endif
	if (NoCompress) {
		// 無圧縮の場合はそのままコピー
		memcpy(ToBuff.get(), FromBuff.get(), OrigLen);
	}
	else {
		DataIO.CurUnpRead = 0;
		DataIO.CurUnpWrite = 0;
		DataIO.UnpVolume = 0;
		DataIO.NextVolumeMissing = false;
		DataIO.SetPackedSizeToRead(CompLen);
	
		DataIO.SetUnpackFromMemory(FromBuff.get(), CompLen);
		DataIO.SetUnpackToMemory(ToBuff.get(), OrigLen);

#if 0	// COMITTONxT_MOD
		memset(Window, 0, MAXWINSIZE);
#else
        // 内部確保領域なので対処不要？
#endif
		Unp->SetDestSize(OrigLen);
		Unp->DoUnpack(RarVersion, false);
	}
	return 0;
}

/*
 * Class:     src_comitton_stream_callLibrary
 * Method:    rarRead
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallJniLibrary_rarRead(JNIEnv *env, jclass obj, jbyteArray orgArray, jint offset, jint size)
{
	if (ToBuff == nullptr) {
		return -1;
	}

	jbyte *data = env->GetByteArrayElements(orgArray, NULL);

	if (OrigLen - ToPos < size) {
		// バッファサイズまでしか読み込まない
		size = OrigLen - ToPos;
	}

	memcpy(&data[offset], &ToBuff[ToPos], size);
	ToPos += size;

	env->ReleaseByteArrayElements(orgArray, data, 0);
	return size;
}

/*
 * Class:     src_comitton_stream_callLibrary
 * Method:    rarInitSeek
 * Signature: (I[II)V
 */
JNIEXPORT jint JNICALL Java_src_comitton_jni_CallJniLibrary_rarInitSeek(JNIEnv *env, jclass obj)
{
	if (ToBuff == nullptr) {
		return -1;
	}

	ToPos = 0;
	return 0;
}

/*
 * Class:     src_comitton_stream_callLibrary
 * Method:    rarClose
 * Signature: ([BI)I
 */
JNIEXPORT void JNICALL Java_src_comitton_jni_CallJniLibrary_rarClose(JNIEnv *env, jclass obj)
{
	ToBuff.reset();
	FromBuff.reset();

	CompLen = 0;
	OrigLen = 0;
	return;
}

}

