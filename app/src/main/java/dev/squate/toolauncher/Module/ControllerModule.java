package dev.squate.toolauncher.Module;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import dev.squate.toolauncher.R;

public class ControllerModule {
    private Context context;
    private View decorView;
    private Map<String, ModuleData> modules = new HashMap<>();
    private String menu2 = "#736584";
    private File configDir;
    private Map<String, String> loadedData = new HashMap<>();

    public static class ModuleData {
        public String name;
        public boolean enabled;
        public boolean hasSettings;
        public LinearLayout settingsPanel;
        public Map<Integer, SliderData> sliders = new HashMap<>();
        public Map<String, Object> values = new HashMap<>();
        public Boolean[] enabledRef;
        public Float[] cpsRef;
        public int bindX = 100;
        public int bindY = 100;
        public boolean bindEnabled = false;
        public PopupWindow bindPopup;
        public boolean bindShowing = false;

        public ModuleData(String name, Boolean[] enabledRef, boolean hasSettings) {
            this.name = name;
            this.enabledRef = enabledRef;
            if (enabledRef != null && enabledRef.length > 0) {
                this.enabled = enabledRef[0];
            }
            this.hasSettings = hasSettings;
        }

        public void addSlider(int id, String name, Float[] valueRef, float min, float max) {
            float initialValue = (valueRef != null && valueRef.length > 0) ? valueRef[0] : min;
            SliderData slider = new SliderData(id, name, initialValue, min, max, valueRef);
            sliders.put(id, slider);
            values.put(name, initialValue);

            if (id == 1) {
                this.cpsRef = valueRef;
            }
        }

        public float getValue(int id) {
            SliderData slider = sliders.get(id);
            if (slider != null) {
                return (float) values.get(slider.displayName);
            }
            return 0;
        }

        public void updateValue(int id) {
            SliderData slider = sliders.get(id);
            if (slider != null && slider.valueRef != null && slider.valueRef.length > 0) {
                slider.valueRef[0] = (float) values.get(slider.displayName);
            }
        }

        public void updateAllValues() {
            for (SliderData slider : sliders.values()) {
                if (slider.valueRef != null && slider.valueRef.length > 0) {
                    slider.valueRef[0] = (float) values.get(slider.displayName);
                }
            }
            if (enabledRef != null && enabledRef.length > 0) {
                enabledRef[0] = this.enabled;
            }
        }

        public void updateEnabled() {
            if (enabledRef != null && enabledRef.length > 0) {
                enabledRef[0] = this.enabled;
            }
        }

        public void syncWithRefs() {
            if (enabledRef != null && enabledRef.length > 0) {
                this.enabled = enabledRef[0];
            }
            for (SliderData slider : sliders.values()) {
                if (slider.valueRef != null && slider.valueRef.length > 0) {
                    float refValue = slider.valueRef[0];
                    this.values.put(slider.displayName, refValue);
                    slider.value = refValue;
                }
            }
        }

        public void resetToDefaults() {
            this.enabled = false;
            this.bindX = 100;
            this.bindY = 100;
            this.bindEnabled = false;
            for (SliderData slider : sliders.values()) {
                slider.value = slider.min;
                this.values.put(slider.displayName, slider.min);
            }
        }
    }

    public static class SliderData {
        public int id;
        public String displayName;
        public float value;
        public float min;
        public float max;
        public Float[] valueRef;

        public SliderData(int id, String displayName, float value, float min, float max, Float[] valueRef) {
            this.id = id;
            this.displayName = displayName;
            this.value = value;
            this.min = min;
            this.max = max;
            this.valueRef = valueRef;
        }
    }

    public void loadModule(Context ctx, View decor) {
        context = ctx;
        decorView = decor;

        File storageDir = Environment.getExternalStorageDirectory();
        configDir = new File(storageDir, "ToolLauncher/config");

        if (!configDir.exists()) {
            boolean created = configDir.mkdirs();
            Log.d("ControllerModule", "Config dir created: " + created + " at " + configDir.getAbsolutePath());
        }
    }

    public void saveConfig(String filename) {
        if (filename == null || filename.isEmpty()) return;
        if (!filename.endsWith(".cfg")) filename = filename + ".cfg";
        if (configDir == null) return;

        if (!configDir.exists()) configDir.mkdirs();

        File configFile = new File(configDir, filename);

        try {
            StringBuilder content = new StringBuilder();
            content.append("{\n");

            int moduleCount = 0;
            for (ModuleData module : modules.values()) {
                moduleCount++;
                module.updateAllValues();

                content.append("  \"").append(module.name).append("\": {\n");
                content.append("    \"enabled\": ").append(module.enabled).append(",\n");
                content.append("    \"bind\": [").append(module.bindX).append(", ").append(module.bindY).append(", ").append(module.bindEnabled).append("]");

                if (!module.sliders.isEmpty()) {
                    content.append(",\n    \"sliders\": {\n");

                    int sliderCount = 0;
                    for (SliderData slider : module.sliders.values()) {
                        sliderCount++;
                        Object value = module.values.get(slider.displayName);
                        if (value == null) value = slider.min;
                        content.append("      \"").append(slider.displayName.toLowerCase().replace(" ", "_")).append("\": ").append(value);
                        if (sliderCount < module.sliders.size()) {
                            content.append(",");
                        }
                        content.append("\n");
                    }

                    content.append("    }\n");
                } else {
                    content.append("\n");
                }

                content.append("  }");
                if (moduleCount < modules.size()) {
                    content.append(",");
                }
                content.append("\n");
            }

            content.append("}");

            FileOutputStream fos = new FileOutputStream(configFile);
            OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            writer.write(content.toString());
            writer.close();

            Log.d("ControllerModule", "Saved config:\n" + content.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadConfig(String filename) {
        loadedData.clear();

        if (!filename.endsWith(".cfg")) filename = filename + ".cfg";
        if (configDir == null) return;

        File configFile = new File(configDir, filename);
        if (!configFile.exists()) return;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
            StringBuilder json = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                json.append(line.trim());
            }
            reader.close();

            String content = json.toString();
            Log.d("ControllerModule", "Loading config: " + content);

            content = content.replace("{", "").replace("}", "");
            String[] moduleBlocks = content.split("\",\"");

            for (String block : moduleBlocks) {
                block = block.trim();
                if (block.isEmpty()) continue;

                String[] lines = block.split(",");
                String currentModule = null;

                for (String item : lines) {
                    item = item.trim().replace("\"", "");
                    if (item.contains(":")) {
                        String[] parts = item.split(":", 2);
                        String key = parts[0].trim();
                        String value = parts[1].trim();

                        if (!key.contains("enabled") && !key.contains("bind") && !key.contains("sliders")) {
                            currentModule = key;
                            continue;
                        }

                        if (currentModule != null) {
                            if (key.equals("enabled")) {
                                loadedData.put(currentModule + "_enabled", value);
                            } else if (key.equals("bind") && value.startsWith("[")) {
                                loadedData.put(currentModule + "_bind", value.replace("[", "").replace("]", ""));
                            } else if (!key.equals("sliders") && !key.isEmpty()) {
                                loadedData.put(currentModule + "_slider_" + key, value);
                            }
                        }
                    }
                }
            }

            applyLoadedDataToAllModules();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyLoadedDataToAllModules() {
        if (loadedData.isEmpty()) return;

        for (ModuleData module : modules.values()) {
            module.resetToDefaults();

            String enabledVal = loadedData.get(module.name + "_enabled");
            if (enabledVal != null) {
                module.enabled = Boolean.parseBoolean(enabledVal);
                if (module.enabledRef != null && module.enabledRef.length > 0) {
                    module.enabledRef[0] = module.enabled;
                }
            }

            String bindVal = loadedData.get(module.name + "_bind");
            if (bindVal != null) {
                String[] bindParts = bindVal.split(",");
                if (bindParts.length == 3) {
                    try {
                        module.bindX = Integer.parseInt(bindParts[0].trim());
                        module.bindY = Integer.parseInt(bindParts[1].trim());
                        module.bindEnabled = Boolean.parseBoolean(bindParts[2].trim());
                    } catch (NumberFormatException e) {}
                }
            }

            for (SliderData slider : module.sliders.values()) {
                String key = module.name + "_slider_" + slider.displayName.toLowerCase().replace(" ", "_");
                String val = loadedData.get(key);
                if (val != null) {
                    try {
                        float floatVal = Float.parseFloat(val);
                        slider.value = floatVal;
                        module.values.put(slider.displayName, floatVal);
                        if (slider.valueRef != null && slider.valueRef.length > 0) {
                            slider.valueRef[0] = floatVal;
                        }
                    } catch (NumberFormatException e) {}
                }
            }
        }

        updateAllBindPopups();
    }

    private void updateAllBindPopups() {
        for (ModuleData module : modules.values()) {
            if (module.bindEnabled && module.bindPopup == null) {
                showBindPopup(module);
            } else if (!module.bindEnabled && module.bindPopup != null) {
                module.bindPopup.dismiss();
                module.bindShowing = false;
                module.bindPopup = null;
            } else if (module.bindEnabled && module.bindPopup != null) {
                module.bindPopup.update(module.bindX, module.bindY, -1, -1);

                GradientDrawable bg = (GradientDrawable) ((LinearLayout) module.bindPopup.getContentView()).getBackground();
                bg.setColor(Color.parseColor(module.enabled ? "#4CAF50" : menu2));
            }
        }
    }

    public ModuleData createModule(String name, Boolean[] enabledRef, boolean hasSettings) {
        if (modules.containsKey(name)) {
            ModuleData existingModule = modules.get(name);
            existingModule.enabledRef = enabledRef;
            if (enabledRef != null && enabledRef.length > 0) {
                existingModule.enabled = enabledRef[0];
            }
            existingModule.syncWithRefs();
            return existingModule;
        }
        ModuleData module = new ModuleData(name, enabledRef, hasSettings);
        modules.put(name, module);
        return module;
    }

    public void addSlider(ModuleData module, int id, String name, Float[] valueRef, float min, float max) {
        if (!module.sliders.containsKey(id)) {
            module.addSlider(id, name, valueRef, min, max);
        } else {
            SliderData existingSlider = module.sliders.get(id);
            existingSlider.valueRef = valueRef;
            if (valueRef != null && valueRef.length > 0) {
                valueRef[0] = (float) module.values.get(name);
            }
        }
    }

    private void styleSeekBar(SeekBar seekBar) {
        try {
            LayerDrawable layer = (LayerDrawable) seekBar.getProgressDrawable();

            Drawable progressDrawable = layer.findDrawableByLayerId(android.R.id.progress);
            if (progressDrawable != null) {
                GradientDrawable progress = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{Color.parseColor("#9C27B0"), Color.parseColor("#FF4081")});
                progress.setCornerRadius(dip(4));
                progress.setSize(dip(0), dip(2));
                layer.setDrawableByLayerId(android.R.id.progress, progress);
            }

            Drawable secondaryProgress = layer.findDrawableByLayerId(android.R.id.secondaryProgress);
            if (secondaryProgress != null) {
                GradientDrawable secondary = new GradientDrawable();
                secondary.setColor(Color.parseColor("#40FFFFFF"));
                secondary.setCornerRadius(dip(4));
                secondary.setSize(dip(0), dip(4));
                layer.setDrawableByLayerId(android.R.id.secondaryProgress, secondary);
            }

            Drawable background = layer.findDrawableByLayerId(android.R.id.background);
            if (background != null) {
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(Color.parseColor("#33000000"));
                bg.setCornerRadius(dip(4));
                bg.setSize(dip(0), dip(4));
                layer.setDrawableByLayerId(android.R.id.background, bg);
            }

            GradientDrawable thumb = new GradientDrawable();
            thumb.setShape(GradientDrawable.OVAL);
            thumb.setColor(Color.parseColor("#FFFFFF"));
            thumb.setSize(dip(16), dip(16));
            seekBar.setThumb(thumb);
            seekBar.setThumbOffset(dip(8));

        } catch (Exception e) {
            seekBar.setProgressDrawable(null);
        }
    }

    private void showBindPopup(ModuleData module) {
        if (module.bindPopup != null) {
            module.bindPopup.dismiss();
            module.bindPopup = null;
        }

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(module.enabled ? "#4CAF50" : menu2));
        bg.setCornerRadius(16f);

        LinearLayout popupContent = new LinearLayout(context);
        popupContent.setOrientation(LinearLayout.VERTICAL);
        popupContent.setGravity(Gravity.CENTER);
        popupContent.setBackground(bg);
        popupContent.setPadding(dip(2), dip(1), dip(2), dip(1));

        String displayText = module.name.length() == 1 ? module.name :
                module.name.charAt(0) + "" + module.name.charAt(module.name.length() / 2);

        TextView bindDisplay = new TextView(context);
        bindDisplay.setText(displayText.toUpperCase());
        bindDisplay.setTextColor(Color.WHITE);
        bindDisplay.setTextSize(14);
        bindDisplay.setTypeface(null, Typeface.BOLD);
        bindDisplay.setGravity(Gravity.CENTER);
        bindDisplay.setPadding(dip(8), dip(4), dip(8), dip(4));

        PopupWindow popup = new PopupWindow(popupContent, dip(40), dip(40));
        popup.setBackgroundDrawable(null);
        popup.setElevation(dip(6));

        bindDisplay.setOnTouchListener(new View.OnTouchListener() {
            private float dx, dy;
            private int initialX, initialY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dx = event.getRawX() - module.bindX;
                        dy = event.getRawY() - module.bindY;
                        initialX = module.bindX;
                        initialY = module.bindY;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        module.bindX = (int) (event.getRawX() - dx);
                        module.bindY = (int) (event.getRawY() - dy);
                        popup.update(module.bindX, module.bindY, -1, -1);
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (Math.abs(module.bindX - initialX) < 5 &&
                                Math.abs(module.bindY - initialY) < 5) {
                            v.performClick();
                        }
                        return true;
                }
                return false;
            }
        });

        bindDisplay.setOnClickListener(v -> {
            module.enabled = !module.enabled;
            if (module.enabledRef != null && module.enabledRef.length > 0) {
                module.enabledRef[0] = module.enabled;
            }
            bg.setColor(Color.parseColor(module.enabled ? "#4CAF50" : menu2));
            popupContent.setBackground(bg);
        });

        popupContent.addView(bindDisplay);
        popup.showAtLocation(decorView, Gravity.NO_GRAVITY, module.bindX, module.bindY);
        module.bindPopup = popup;
        module.bindShowing = true;
    }

    public void addModule(LinearLayout root, ModuleData module) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dip(2), 0, dip(2), 0);
        container.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        header.setPadding(dip(4), dip(2), dip(4), dip(2));

        TextView title = new TextView(context);
        title.setText(module.name);
        title.setTextColor(0xFFFFFFFF);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(14);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));

        LinearLayout rightContainer = new LinearLayout(context);
        rightContainer.setOrientation(LinearLayout.HORIZONTAL);
        rightContainer.setGravity(Gravity.CENTER_VERTICAL);

        if (module.hasSettings) {
            ImageView settingsBtn = new ImageView(context);
            settingsBtn.setImageResource(R.drawable.settings);
            settingsBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);
            settingsBtn.setLayoutParams(new LinearLayout.LayoutParams(dip(22), dip(22)));
            settingsBtn.setColorFilter(Color.WHITE);
            rightContainer.addView(settingsBtn);

            View spacer = new View(context);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(dip(4), dip(4)));
            rightContainer.addView(spacer);
        }

        ImageView toggle = new ImageView(context);
        toggle.setScaleType(ImageView.ScaleType.FIT_CENTER);
        toggle.setLayoutParams(new LinearLayout.LayoutParams(dip(36), dip(36)));

        View.OnClickListener toggleListener = v -> {
            module.enabled = !module.enabled;
            toggle.setImageResource(module.enabled ? R.drawable.switch_on : R.drawable.switch_off);
            toggle.setColorFilter(Color.parseColor(module.enabled ? "#4CAF50" : "#F44336"));
            if (module.enabledRef != null && module.enabledRef.length > 0) {
                module.enabledRef[0] = module.enabled;
            }

            if (module.bindEnabled && module.bindShowing && module.bindPopup != null) {
                GradientDrawable bg = (GradientDrawable) ((LinearLayout) module.bindPopup.getContentView()).getBackground();
                bg.setColor(Color.parseColor(module.enabled ? "#4CAF50" : menu2));
            }
        };

        toggle.setImageResource(module.enabled ? R.drawable.switch_on : R.drawable.switch_off);
        toggle.setColorFilter(Color.parseColor(module.enabled ? "#4CAF50" : "#F44336"));

        title.setOnClickListener(toggleListener);
        toggle.setOnClickListener(toggleListener);
        rightContainer.addView(toggle);
        header.addView(title);
        header.addView(rightContainer);
        container.addView(header);

        LinearLayout settingsPanel = new LinearLayout(context);
        settingsPanel.setOrientation(LinearLayout.VERTICAL);
        settingsPanel.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        settingsPanel.setPadding(dip(8), dip(2), dip(8), dip(2));
        settingsPanel.setVisibility(View.GONE);

        LinearLayout bindLayout = new LinearLayout(context);
        bindLayout.setOrientation(LinearLayout.HORIZONTAL);
        bindLayout.setGravity(Gravity.CENTER_VERTICAL);
        bindLayout.setPadding(0, 0, 0, dip(2));

        TextView bindText = new TextView(context);
        bindText.setTextSize(13);
        bindText.setTypeface(null, Typeface.BOLD);
        bindText.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));

        bindText.setOnClickListener(v -> {
            module.bindEnabled = !module.bindEnabled;
            if (module.bindEnabled) {
                showBindPopup(module);
            } else if (module.bindPopup != null) {
                module.bindPopup.dismiss();
                module.bindShowing = false;
                module.bindPopup = null;
            }
            updateBindText(bindText, module);
        });

        bindLayout.addView(bindText);
        settingsPanel.addView(bindLayout);

        for (SliderData slider : module.sliders.values()) {
            LinearLayout sliderContainer = new LinearLayout(context);
            sliderContainer.setOrientation(LinearLayout.VERTICAL);
            sliderContainer.setLayoutParams(new LinearLayout.LayoutParams(-1, dip(46)));
            sliderContainer.setPadding(0, dip(1), 0, dip(1));

            TextView label = new TextView(context);
            label.setTextColor(0xFFCCCCCC);
            label.setTextSize(11);
            label.setTypeface(null, Typeface.BOLD);

            SeekBar seek = new SeekBar(context);
            seek.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            seek.setMax(1000);

            styleSeekBar(seek);

            float min = slider.min;
            float max = slider.max;
            float currentVal = (float) module.values.get(slider.displayName);

            label.setText(slider.displayName + ": " + String.format("%.1f", currentVal));
            seek.setProgress((int) ((currentVal - min) / (max - min) * 1000));

            final int sliderId = slider.id;
            seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    float newVal = min + (max - min) * progress / 1000f;
                    module.values.put(slider.displayName, newVal);
                    SliderData updatedSlider = module.sliders.get(sliderId);
                    if (updatedSlider != null) {
                        updatedSlider.value = newVal;
                        if (updatedSlider.valueRef != null && updatedSlider.valueRef.length > 0) {
                            updatedSlider.valueRef[0] = newVal;
                        }
                    }
                    label.setText(slider.displayName + ": " + String.format("%.1f", newVal));
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            sliderContainer.addView(label);
            sliderContainer.addView(seek);
            settingsPanel.addView(sliderContainer);
        }

        if (module.hasSettings) {
            ((ImageView) rightContainer.getChildAt(0)).setOnClickListener(v -> {
                settingsPanel.setVisibility(settingsPanel.getVisibility() == View.VISIBLE ?
                        View.GONE : View.VISIBLE);
            });
        }

        module.settingsPanel = settingsPanel;
        container.addView(settingsPanel);
        root.addView(container);

        updateBindText(bindText, module);

        if (module.bindEnabled && module.bindPopup == null) {
            showBindPopup(module);
        }
    }

    private void updateBindText(TextView bindText, ModuleData module) {
        bindText.setText(Html.fromHtml("<b><font color='#FFFFFF'>Bind : </font></b><b><font color='" +
                (module.bindEnabled ? "#4CAF50" : "#FF0000") + "'>" +
                (module.bindEnabled ? "ON" : "OFF") + "</font></b>"));
    }

    private int dip(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }
}