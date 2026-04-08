package dev.squate.toolauncher.Utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;
import java.io.File;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import dev.squate.toolauncher.Module.MENU;

public class Utils {

    static {
        System.loadLibrary("toollauncher");
    }
    public GradientDrawable bgStart;
    Context ctx;

    private static Utils INSTANCE;

    public static Utils getInstance(){
        if(INSTANCE == null){
            INSTANCE = new Utils();
        }
        return INSTANCE;
    }


    public void toast(Context ctx, String text){
        Toast.makeText(ctx, text, Toast.LENGTH_LONG).show();
    }

    public void initUtils(){
        bgStart = new GradientDrawable();
        bgStart.setColor(Color.parseColor("#991A1A"));
        bgStart.setCornerRadius(12f);

    }

    public void copyFile(String patch1, String patch2) {
        try {
            File external = Environment.getExternalStorageDirectory();
            String basePath = external.getAbsolutePath();

            File sourceFile = new File(basePath + patch1);
            File destFile = new File(basePath + patch2);

            if (!sourceFile.exists()) {
                toast(ctx, "Исходный файл не существует");
                return;
            }

            File parent = destFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileInputStream inputStream = new FileInputStream(sourceFile);
                 FileOutputStream outputStream = new FileOutputStream(destFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                toast(ctx, "Файл скопирован успешно");
            }

        } catch (IOException e) {
            e.printStackTrace();
            toast(ctx, "Ошибка копирования: " + e.getMessage());
        }
    }

    public void createPath(String path) {
        try {
            File external = Environment.getExternalStorageDirectory();
            String basePath = external.getAbsolutePath();

            File directory = new File(basePath + path);

            if (!directory.exists()) {
                if (directory.mkdirs()) {

                } else {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            toast(ctx, "Ошибка: " + e.getMessage());
        }
    }

    public void createFile(String patch, String file, String content) {
        try {
            File external = Environment.getExternalStorageDirectory();
            File files = new File(external.getAbsolutePath() + patch + file);
            File parent = files.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileWriter writeF = new FileWriter(files)) {
                writeF.write(content);
            }

        } catch (IOException e) {
            e.printStackTrace();
            toast(ctx, "Ошибка: " + e.getMessage());
        }
    }

    void ToolLauncher(){
        MENU menu = MENU.getInstance();
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(menu.killauraEnabled[0]){

                }
                handler.postDelayed(this, 1000 / 20);
            }
        };
        handler.postDelayed(runnable, 1000 / 20);
    }
    public int dips(Context ctx, int value) {
        return Math.round(value * ctx.getResources().getDisplayMetrics().scaledDensity);
    }

}
