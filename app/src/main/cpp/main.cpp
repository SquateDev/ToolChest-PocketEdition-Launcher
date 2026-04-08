#include <jni.h>
#include <string>
#include <dlfcn.h>
#include <../main.h>
#include "substrate/SubstrateHook.h"
#include <../Includes/util.h>
#include <../Pointer.h>
#include <android/log.h>
#include <sys/stat.h>
#include "../Local/PlayerLocal.h"
#include <android/log.h>
#define TAG "toollauncher"
#define LOGE(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

PlayerLocal* playerLocal = new PlayerLocal();

LocalPlayer* getLocalPlayers() {
    return (LocalPlayer* )g_points.instance->getLocalPlayer();
}

Entity* getEntity(){
    LocalPlayer* localPlayer = ((LocalPlayer*)getLocalPlayers());
    return (Entity*) localPlayer;
}

void (*player_original)(void* _this, void* entity, void* vec3, float f1, float f2);
void player_hook(void* _this, void* entity, void* vec3, float f1, float f2) {
    player_original(_this, entity, vec3, f1, f2);
    playerLocal->tick(entity, getEntity());
}

void (*tool_Minecraft)(MinecraftGame*);
void tool_Minecraft_hook(MinecraftGame* game){
    tool_Minecraft(game);
    if(game) g_points.toolGame = game;
    if(game) g_points.instance = game->getPrimaryClientInstance();
    if(game && g_points.instance) g_points.level = g_points.instance->getLevel();
    if(game && g_points.instance) g_points.myLocal = g_points.instance->getLocalPlayer();
}

extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGE("INIT 2LIB");

    playerLocal->initLocal();

    void* handle = dlopen("libminecraftpe.so", RTLD_NOW);

    MSHookFunction(dlsym(handle, "_ZN13MinecraftGame6updateEv"), (void*)&tool_Minecraft_hook, (void**)&tool_Minecraft);
    MSHookFunction(dlsym(handle, "_ZN14PlayerRenderer6renderER6EntityRK4Vec3ff"), (void*)&player_hook, (void**)&player_original);

    dlclose(handle);
    return JNI_VERSION_1_6;
}
