#include <../main.h>
#include <../Pointer.h>
#include <../Includes/util.h>
#include <android/log.h>
#include "PlayerLocal.h"
#include <dlfcn.h>
#include <android/log.h>
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wnon-pod-varargs"
#define TAG "toollauncher"
#define LOGE(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
Pointer* pointer = new Pointer();
ClientInputCallbacks* clientinputcallbacks;
ClientInstance* pClientInstance;


// вызов методы
static Vec3* (*_Entity_getPos)(void*);
std::string (*getScreen)(void*);
static Vec2* (*_Entity_getRot)(void*);
static void (*Entity_setSize)(Entity*, float, float);


// хлам
Vec3* my; Vec3* player;
float distKillaura = 9.0f; void* targetKillaura = nullptr;


void PlayerLocal::initLocal(){
    void* handle = dlopen("libminecraftpe.so", RTLD_LAZY);
    LOGE("LIB2 Open");

    _Entity_getPos = (Vec3* (*)(void*))dlsym(handle, "_ZNK6Entity6getPosEv");
    Entity_setSize = (void (*)(Entity*, float, float))dlsym(handle, "_ZN6Entity7setSizeEff");
    getScreen = (std::string (*)(void*)) dlsym(handle, "_ZN13MinecraftGame13getScreenNameEv");


    dlclose(handle);
    LOGE("LIB2 Close");
}

LocalPlayer* PlayerLocal::getLocalPlayers() {
    return (LocalPlayer*) pointer->myLocal;
}

Entity* PlayerLocal::getEntity(){
    LocalPlayer* localPlayer = getLocalPlayers();
    return (Entity*) localPlayer;
}

float distanceTo(float x, float y, float z, float xx, float yy, float zz){
    float dx = xx - x;
    float dy = yy - y;
    float dz = zz - z;

    return sqrtf(dx*dx + dy*dy + dz*dz);
}
void PlayerLocal::tick(void* entity, Entity* myUK) {
    if(entity && entity != myUK) player = _Entity_getPos(entity);
    if(myUK && myUK != entity) my = _Entity_getPos(myUK);
    if(distanceTo(my->x, my->y, my->z, player->x, player->y, player->z) <= distKillaura){
        targetKillaura = entity;
    } else {
        targetKillaura = nullptr;
    }
}

void PlayerLocal::setVelX(float x) {
    Entity* ent = PlayerLocal::getEntity();
    if(ent && g_points.level && g_points.instance){
        ent->motionX = x;
    }
}
void PlayerLocal::setVelY(float y) {
    Entity* ent = PlayerLocal::getEntity();
    if(ent && g_points.level && g_points.instance){
        ent->motionY = y;
    }
}
void PlayerLocal::setVelZ(float z) {
    Entity* ent = PlayerLocal::getEntity();
    if(ent && g_points.level && g_points.instance){
        ent->motionZ = z;
    }
}
void PlayerLocal::setVel(float x, float y, float z) {
    Entity* ent = PlayerLocal::getEntity();
    if(ent && g_points.level && g_points.instance){
        ent->motionX = x;
        ent->motionY = y;
        ent->motionZ = z;
    }
}

void PlayerLocal::setPos(float xd, float yd, float zd) {
    Entity* ent = PlayerLocal::getEntity();
    if(ent && g_points.level && g_points.instance){
        ent->x = xd;
        ent->y = yd;
        ent->z = zd;
    }
}

void PlayerLocal::attack(float dist) {
    clientinputcallbacks = (ClientInputCallbacks*) g_points.instance;
    pClientInstance = g_points.instance;
    if(distKillaura != dist) distKillaura = dist;
    if(targetKillaura == nullptr || g_points.instance->getLevel() == nullptr) return;
    HitResult* hitResult = (HitResult*)&g_points.instance->getLevel()->getHitResult();
    hitResult->type = 1;
    hitResult->entity = (Entity*)targetKillaura;
    if (clientinputcallbacks && pClientInstance) {
        clientinputcallbacks->handleDestoryOrAttackButtonPress(*pClientInstance);
    }
}


#pragma clang diagnostic pop