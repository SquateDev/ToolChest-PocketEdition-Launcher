#include <jni.h>
#include "../Local/PlayerLocal.h"
PlayerLocal* localP;

extern "C" JNIEXPORT void JNICALL
Java_dev_squate_toolauncher_API_LocalPlayer_setPos(JNIEnv* env, jobject, jfloat x, jfloat y, jfloat z) {
    localP->setPos(x, y, z);
}

extern "C" JNIEXPORT void JNICALL
Java_dev_squate_toolauncher_API_LocalPlayer_setVelocityX(JNIEnv* env, jobject, jfloat x) {
    localP->setVelX(x);
}
extern "C" JNIEXPORT void JNICALL
Java_dev_squate_toolauncher_API_LocalPlayer_setVelocityY(JNIEnv* env, jobject, jfloat y) {
    localP->setVelY(y);
}
extern "C" JNIEXPORT void JNICALL
Java_dev_squate_toolauncher_API_LocalPlayer_setVelocityZ(JNIEnv* env, jobject, jfloat z) {
    localP->setVelY(z);
}
extern "C" JNIEXPORT void JNICALL
Java_dev_squate_toolauncher_API_LocalPlayer_setVelocity(JNIEnv* env, jobject, jfloat x, jfloat y, jfloat z) {
    localP->setVel(x, y, z);
}
extern "C"
JNIEXPORT void JNICALL
Java_dev_squate_toolauncher_API_LocalPlayer_setRot(JNIEnv *env, jobject thiz, jfloat yaw,
                                                   jfloat pitch) {

}
extern "C"
JNIEXPORT jstring JNICALL
Java_dev_squate_toolauncher_API_LocalPlayer_getScreenName(JNIEnv *env, jobject thiz) {

}
extern "C"
JNIEXPORT jlong JNICALL
Java_dev_squate_toolauncher_API_LocalPlayer_getDistance(JNIEnv *env, jobject thiz, jfloat dist) {

}
extern "C"
JNIEXPORT void JNICALL
Java_dev_squate_toolauncher_API_LocalPlayer_attack(JNIEnv *env, jobject thiz, jfloat dist) {
    localP->attack(dist);
}