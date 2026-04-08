package dev.squate.toolauncher.API;

public class LocalPlayer {
    private static LocalPlayer instance;

    public static LocalPlayer getInstance(){
        if(instance == null){
            instance = new LocalPlayer();
        }
        return instance;
    }
    public native void setPos(float x, float y, float z);
    public native void setVelocityX(float x);
    public native void setVelocityY(float y);
    public native void setVelocityZ(float z);
    public native void setVelocity(float x, float y, float z);

    public native void setRot(float yaw, float pitch);

    public native String getScreenName();

    public native long getDistance(float dist);

    public native void attack(float dist);

}
