#ifndef TOOLLAUNCHER_MAIN_H
#define TOOLLAUNCHER_MAIN_H

#include <../Includes/util.h>
#include <string>
#include <vector>
#include <sys/time.h>

#ifndef M_PI
#define M_PI 3.14159265358979323846f
#endif

class Level;
class Timer;
namespace mce {
    class TextureGroup;
    class TexturePtr;
    class Texture;
}
class Mob;
class Options;
class AbstractScreen;
class ScreenChooser;
class GuiData;
class MinecraftCommands;
class LevelRenderer;
class ResourcePackManager;
class ClientInstance;
class EntityRenderDispatcher;
class ItemRenderer;
class UIProfanityContext;
class ServerNetworkHandler;
class SceneStack;
class SceneFactory;
class Player;
class MinecraftGame;
class ArmorSlot;
class ItemInstance;
class BlockPos;
class Block;
class Font;
class Entity;

class GameMode {
public:
    void buildBlock(Player&, BlockPos, signed char);
};

class ShaderColor {
public:
    void setColor(Color const&);
};

class MinecraftScreenModel {
public:
    void sendChatMessage(std::string const&);
};

struct EntityUniqueID {};

class Minecraft {
public:
    Level* getLevel() const;
    Timer* getTimer();
    MinecraftCommands* getCommands();
    GameMode* getGameMode();
    ResourcePackManager* getResourceLoader();
    void setGameModeReal(GameType);
};

class Abilities {
public:
    void setAbility(std::string const&, bool);
    void setAbility(std::string const&, float);
    void getBool(std::string const&) const;
    void getFloat(std::string const&) const;
};

class LocalPlayer {
public:
    void move(Vec3 const&);
    void closeScreen();
    void setSneaking(bool);
    void setSprinting(bool);
    void openContainer(EntityUniqueID const&);
    void setArmor(ArmorSlot, ItemInstance const&);
    void setPos(Vec3 const&);
    void chat(std::string const&);
    int64_t getFieldOfViewModifier();
    void swing();
    void sendInput();
    void sendPosition();
    void openInventory();
};

class Dimension {
public:
    Entity* getNearestPlayer(Vec3 pos, float distance);
};

class Entity {
public:
    char filler3[72-4];
    float x;
    float y;
    float z;
    char filler2[108-84];
    float motionX;
    float motionY;
    float motionZ;
    float pitch;
    float yaw;
    void* setUniqueID(EntityUniqueID);
    void lerpMotion(Vec3 const&);
    Vec3 getVelocity() const;
    Vec2 getRotation() const;
    const char* getNameTag() const;
    uint16_t getUniqueID() const;
    void isInLava();
    int isInvisible() const;
    int isImmobile() const;
    void isInWater() const;
    Vec3 getPos() const;
    void setCanClimb(bool);
    void setTarget(Entity*);
    void setClimbing(bool);
    void setRot(Vec2 const&);
    Dimension getDimension() const;
};

typedef struct {
    int type;
    unsigned char side;
    int x;
    int y;
    int z;
    Vec3 hitVec;
    void* entity;
    unsigned char filler1;
} HitResult;

class Player : public Entity {
public:
    void setSpeed(float);
    void setFieldOfViewModifier(float);
    void attack(Entity&);

    void tick(Entity *entity);
};

class ClientInputCallbacks {
public:
    void handleBuildOrAttackButtonPress(ClientInstance&);
    void handleDestoryOrAttackButtonPress(ClientInstance&);
    void handleBuildActionButtonRelease(ClientInstance&);
    void handleAttackActionButtonRelease(ClientInstance&);
    void handleBuildOrInteractButtonPress(ClientInstance&);
    void handleInteractButtonPress(ClientInstance&);
};

class ScreenStack {
public:
    std::string getScreenName() const;
};

class Level {
public:
    void setTime(int);
    Player* getPlayer(EntityUniqueID id);
    Vec3 getPos() const;
    HitResult const& getHitResult();
    int getTime() const;
    void addParticle(ParticleType, Vec3 const&, Vec3 const&, int);
    long fetchEntity(EntityUniqueID, bool);
};

class GuiData {
public:
    void displayClientMessage(std::string const&);
    void showTipMessage(std::string const&);
    void setSubtitle(std::string const&);
    void displayChatMessage(std::string const&, std::string const&);
    void setTitle(std::string const&);
    mce::TexturePtr const& getGuiTex();
};

class IClientInstance {
};

class ClientInstance : public IClientInstance {
public:
    MinecraftGame* getMinecraftGame() const;
    Minecraft* getServerData();
    LocalPlayer* getLocalPlayer();
    mce::TextureGroup& getTextures() const;
    void setCameraEntity(Entity*);
    void setCameraTargetEntity(Entity*);
    Options* getOptions();
    AbstractScreen* getScreen();
    ScreenChooser& getScreenChooser() const;
    GuiData* getGuiData();
    void onResourcesLoaded();
    LevelRenderer* getLevelRenderer() const;
    void play(std::string const&, Vec3 const&, float, float);
    void _startLeaveGame();
    Level* getLevel();
    EntityRenderDispatcher& getEntityRenderDispatcher();
    UIProfanityContext const& getUIProfanityContext() const;
    mce::Texture const& getUITexture();
    SceneStack& getClientSceneStack() const;
    SceneFactory& getSceneFactory() const;
    Level* getLocalServerLevel();
    Entity* getEntityData();
    Player* getPlayerLevel() const;
    ClientInstance* getMinecraftGame();
    int getInput() const;
    int getCurrentInputMode() const;

};

class IMinecraftGame {
};

class MinecraftGame : public IMinecraftGame {
public:
    Level* getLocalServerLevel() const;
    ClientInstance* getPrimaryClientInstance();
    void updateFoliageColors();
    ServerNetworkHandler* getServerNetworkHandler();
    Minecraft* getServerInstance();
    ClientInstance* getMinecraftGame();
    MinecraftGame* getScreenName();
};

#define MinecraftClient ClientInstance

#endif //TOOLLAUNCHER_MAIN_H
