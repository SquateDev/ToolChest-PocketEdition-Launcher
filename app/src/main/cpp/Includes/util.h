//
// Created by sosan on 01.03.2026.
//

#ifndef TOOLLAUNCHER_UTIL_H
#define TOOLLAUNCHER_UTIL_H

#include <string>

struct Vec2 {
    float x,y;
    Vec2(float _x, float _y): x(_x), y(_y){};
};
struct Vec3 {
    float x,y,z;
    Vec3(float _x, float _y, float _z): x(_x), y(_y), z(_z){};
};

enum GameType {
};


enum ParticleType {
};
ParticleType ParticleTypeFromString(std::string const&);

struct Color {
    float r;
    float g;
    float b;
    float a;
};

struct Rotation {
    float yaw;
    float pitch;
};

enum Key {
    JUMP = -3,
    SHIFT = -2,
    JUMP_IN_FLIGHT = 0,
    FORWARD = 1,
    BACK = 2,
    LEFT = 3,
    RIGHT = 4,
    LEFT_TOP = 5,
    RIGHT_TOP = 6
};


#endif //TOOLLAUNCHER_UTIL_H
