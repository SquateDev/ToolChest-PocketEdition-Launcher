#ifndef TOOLLAUNCHER_POINTER_H
#define TOOLLAUNCHER_POINTER_H
#include <../main.h>


class Pointer {
public:
    Entity* myEnt = nullptr;
    MinecraftGame* toolGame = nullptr;
    Level* level = nullptr;
    ClientInstance* instance = nullptr;
    ClientInputCallbacks* clientInput = nullptr;
    Player* myPla = nullptr;
    LocalPlayer* myLocal = nullptr;
};

extern Pointer g_points;

#endif