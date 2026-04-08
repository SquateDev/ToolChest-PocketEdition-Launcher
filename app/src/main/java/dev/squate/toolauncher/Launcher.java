package dev.squate.toolauncher;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.mojang.minecraftpe.MainActivity;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import dev.squate.toolauncher.Module.ControllerModule;
import dev.squate.toolauncher.Module.GUI;
import dev.squate.toolauncher.Module.MENU;
import dev.squate.toolauncher.Utils.Utils;

public class Launcher extends MainActivity {
    static {
        System.loadLibrary("toollauncher");
    }
    GUI gui = new GUI();
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        new Handler().postDelayed(() -> {
            if(getBaseContext() != null && getWindow().getDecorView() != null){
                gui.load(getBaseContext());
                gui.GUI_open(getBaseContext(), getWindow().getDecorView().getRootView());
            }
        }, 200);
    }
}