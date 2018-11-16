#include <jni.h>
#include <string>


//extern "C" JNIEXPORT jint*
//JNICALL
//Java_com_ck_utils_CarmeraDataDone_decodeYUV420SPJni(
//        JNIEnv *env,
//        jobject /* this */, jint oneNum, jint twoNum) {
//    jint all = oneNum * twoNum;
//    std::string hello = "Hello from C++";
//    return all;
//}
//Java_com_ck_utils_CarmeraDataDone_decodeYUV420SPJni(
//        JNIEnv *env,
//        jobject /* this */, jbyteArray yuv420sp , jint width, jint hight,jintArray rgbbuf) {
//
//    jbyte* array = env->GetByteArrayElements(yuv420sp,NULL);
//    int arraysize = env ->GetArrayLength(yuv420sp);
//    char buf[arraysize+1];
//    int i = 0;
//    for(i = 0; i < arraysize; i++){
//        buf[i] = array[i];
//    }
//    buf[arraysize] = '\0';
//
//}

int* decodeYUV420SP(int* rgbBuf, char* yuv420sp, int width, int height)
{
    int frameSize = width * height;
    int i = 0, y = 0;
    int uvp = 0, u = 0, v = 0;
    int y1192 = 0, r = 0, g = 0, b = 0;
    for (int j = 0, yp = 0; j < height; j++)
    {
        uvp = frameSize + (j >> 1) * width;
        u = 0;
        v = 0;
        for (i = 0; i < width; i++, yp++)
        {
            y = (0xff & ((int) yuv420sp[yp])) - 16;
            if (y < 0) y = 0;
            if ((i & 1) == 0) {
                v = (0xff & yuv420sp[uvp++]) - 128;
                u = (0xff & yuv420sp[uvp++]) - 128;
            }
            y1192 = 1192 * y;
            r = (y1192 + 1634 * v);
            g = (y1192 - 833 * v - 400 * u);
            b = (y1192 + 2066 * u);

            if (r < 0) r = 0; else if (r > 262143) r = 262143;
            if (g < 0) g = 0; else if (g > 262143) g = 262143;
            if (b < 0) b = 0; else if (b > 262143) b = 262143;

            rgbBuf[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
        }
    }
    return rgbBuf;
}

extern "C"
JNIEXPORT jintArray
JNICALL
/**
 * 此方法为将摄像机的流转为int类型数组
 * @param env
 * @param type
 * @param yuv420sp_
 * @param width
 * @param hight
 * @param buff_
 * @return
 */
Java_com_ck_utils_CarmeraDataDone_decodeYUV420SPJni(JNIEnv *env, jclass type, jbyteArray yuv420sp_,
                                                    jint width, jint hight, jintArray buff_) {
    jint *buff = env->GetIntArrayElements(buff_, NULL);
    jbyte* array = env->GetByteArrayElements(yuv420sp_,NULL);

    int arraysize = env ->GetArrayLength(yuv420sp_);
    char buf[arraysize+1];
    int i = 0;
    for(i = 0; i < arraysize; i++){
        buf[i] = array[i];
    }
    buf[arraysize] = '\0';
    // TODO
    int* a = decodeYUV420SP(buff,buf,width,hight);
    int length = hight*width;
    jintArray res = env ->NewIntArray(length);
    env ->SetIntArrayRegion(res,0,length,a);
    env->ReleaseIntArrayElements(buff_, buff, 0);
    env->ReleaseByteArrayElements(yuv420sp_, array, 0);
    return res ;
}
