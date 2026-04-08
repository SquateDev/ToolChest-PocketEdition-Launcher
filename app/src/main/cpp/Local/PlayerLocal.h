#ifndef TOOLLAUNCHER_PLAYERLOCAL_H
#define TOOLLAUNCHER_PLAYERLOCAL_H
#include <../main.h>

class PlayerLocal {
public:
    static LocalPlayer* getLocalPlayers();
    static Entity* getEntity();
    static void setVelX(float x);
    static void setVelY(float x);
    static void setVelZ(float x);
    static void setVel(float x, float y, float z);
    static void setPos(float x, float y, float z);
    Entity* featchID(long uuid);
    void attack(float  dist);
    void initLocal();
    void tick(void *entity, Entity* uk);
};
#endif