package dev.squate.toolauncher.Module;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import dev.squate.toolauncher.API.LocalPlayer;
import dev.squate.toolauncher.R;
import dev.squate.toolauncher.Utils.Utils;

public class MENU {
    private Context context;
    private View rootView;
    Utils utils = Utils.getInstance();
    LocalPlayer localPlayer = LocalPlayer.getInstance();
    private GradientDrawable bgCategory;
    private GradientDrawable bgContent;
    private GradientDrawable bgBanner;
    private int padding = 12;
    private int margin = 12;
    private int smallPadding = 6;
    private int iconSize = 26;
    private String colorPrimary = "#4b4453";
    private String colorSecondary = "#736584";
    private String[] titles = {"Combat", "Movement", "Visual", "Other", "Config"};
    private int[] icons = {R.drawable.combat, R.drawable.movement, R.drawable.visual, R.drawable.other, R.drawable.config};
    private int selectedCategory = 0;
    private ViewFlipper viewFlipper;
    private LinearLayout[] categoryLayouts;
    private float scaleAnim = 0.09f;
    private LinearLayout categoryPanel;
    public LinearLayout mainContainer;
    public ControllerModule controllerModule = new ControllerModule();
    public Boolean[] killauraEnabled = new Boolean[]{false};
    Float[] killauraC = new Float[]{2.0f}, killauraD = new Float[]{8.0f};

    private static MENU instance;
    int indexs = 0, i = 1;


   public static MENU getInstance() {
       if (instance == null)  {
           instance = new MENU();
       }
       return instance;
   }
    public void loadMenu(Context ctx, View view) {
        context = ctx;
        rootView = view;
        initDrawables();
        createMenu();
        if(i != 0) i = 0; tick();
    }

    void tick(){
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(killauraEnabled[0]){
                    localPlayer.attack(9f);
                }
                handler.postDelayed(this, 50);
            }
        };
        handler.postDelayed(runnable, 50);
    }
    private void initDrawables() {
        bgCategory = new GradientDrawable();
        bgCategory.setCornerRadius(dip(6));
        bgCategory.setColor(Color.parseColor(colorPrimary));

        bgContent = new GradientDrawable();
        bgContent.setCornerRadius(dip(6));
        bgContent.setColor(Color.parseColor(colorSecondary));

        bgBanner = new GradientDrawable();
        float[] radii = {dip(9), dip(9), dip(9), dip(14), 0, 0, 0, 0};
        bgBanner.setCornerRadii(radii);
        bgBanner.setColor(Color.parseColor(colorSecondary));
    }

    private void createMenu() {
        mainContainer = new LinearLayout(context);
        mainContainer.setBackground(bgCategory);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(margin, margin, margin, margin);

        mainContainer.addView(createBanner());
        mainContainer.addView(createSpacer(6));

        LinearLayout contentRow = new LinearLayout(context);
        contentRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        contentRow.setLayoutParams(rowParams);

        contentRow.addView(createCategoryPanel());

        View spacer = new View(context);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(dip(6), LinearLayout.LayoutParams.MATCH_PARENT));
        contentRow.addView(spacer);

        contentRow.addView(createModulePanel());
        mainContainer.addView(contentRow);

        showPopup(mainContainer);
    }

    private LinearLayout createBanner() {
        LinearLayout banner = new LinearLayout(context);
        banner.setBackground(bgBanner);
        banner.setOrientation(LinearLayout.HORIZONTAL);
        banner.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams bannerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip(34));
        banner.setLayoutParams(bannerParams);
        banner.setPadding(dip(padding/3), dip(padding/3), dip(padding/3), dip(padding/3));

        LinearLayout leftSpacer = new LinearLayout(context);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        leftSpacer.setLayoutParams(spacerParams);
        leftSpacer.setPadding(dip(8), dip(8), dip(8), dip(8));
        banner.addView(leftSpacer);

        TextView title = new TextView(context);
        title.setText(Html.fromHtml("<b><font color='#FFFFFF'>ToolLauncher </font></b><b><font color='#FF0000'>V1.0</font></b>"));
        title.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        title.setTextSize(sp(5));
        title.setGravity(Gravity.CENTER);
        banner.addView(title);

        LinearLayout rightSpacer = new LinearLayout(context);
        rightSpacer.setLayoutParams(spacerParams);
        rightSpacer.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        rightSpacer.setPadding(dip(3), dip(3), dip(3), dip(3));
        banner.addView(rightSpacer);

        ImageView github = new ImageView(context);
        github.setImageResource(R.drawable.github);
        github.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams githubParams = new LinearLayout.LayoutParams(dip(22), dip(22));
        github.setLayoutParams(githubParams);
        rightSpacer.addView(github);

        return banner;
    }

    private LinearLayout createCategoryPanel() {
        categoryPanel = new LinearLayout(context);
        categoryPanel.setBackground(bgContent);
        categoryPanel.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams panelParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        categoryPanel.setLayoutParams(panelParams);
        categoryPanel.setPadding(dip(padding), dip(padding), dip(padding), dip(padding));

        for (int i = 0; i < titles.length; i++) {
            if (i > 0) {
                View spacer = new View(context);
                spacer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip(4)));
                categoryPanel.addView(spacer);
            }
            categoryPanel.addView(createCategoryItem(i));
        }
        return categoryPanel;
    }

    private LinearLayout createCategoryItem(int index) {
        LinearLayout item = new LinearLayout(context);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        item.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        if (index < titles.length - 1) item.setPadding(0, 0, 0, dip(13));

        ImageView icon = new ImageView(context);
        icon.setImageResource(icons[index]);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);

        int size = dip(iconSize);
        int imgSize = size;
        if (index == 0) {
            imgSize = size - 10;
            icon.setTranslationX(dip(4));
        } else if (index == 1) {
            imgSize = size + 12;
        }
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(imgSize, imgSize);
        icon.setLayoutParams(iconParams);
        icon.setId(index + 1000);

        TextView text = new TextView(context);
        text.setText("  " + titles[index] + "  ");
        text.setTextColor(Color.WHITE);
        text.setTextSize(12);
        text.setId(index + 2000);
        text.setTypeface(null, Typeface.BOLD);
        text.setGravity(Gravity.CENTER_VERTICAL);
        if (index == 0) text.setTranslationX(dip(5));

        if (index == selectedCategory) {
            icon.setColorFilter(Color.RED);
            text.setTextColor(Color.RED);
            icon.setScaleX(1.0f + scaleAnim);
            icon.setScaleY(1.0f + scaleAnim);
            text.setScaleX(1.0f + scaleAnim);
            text.setScaleY(1.0f + scaleAnim);
        }

        final int categoryIndex = index;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < titles.length; i++) {
                    ImageView img = categoryPanel.findViewWithTag("icon_" + i);
                    TextView txt = categoryPanel.findViewWithTag("text_" + i);
                    if (img != null && txt != null) {
                        if (i == categoryIndex) {
                            img.setColorFilter(Color.RED);
                            txt.setTextColor(Color.RED);
                            img.setScaleX(1.0f + scaleAnim);
                            img.setScaleY(1.0f + scaleAnim);
                            txt.setScaleX(1.0f + scaleAnim);
                            txt.setScaleY(1.0f + scaleAnim);
                            indexs = categoryIndex;
                        } else {
                            img.setColorFilter(null);
                            txt.setTextColor(Color.WHITE);
                            img.setScaleX(1.0f);
                            img.setScaleY(1.0f);
                            txt.setScaleX(1.0f);
                            txt.setScaleY(1.0f);
                        }
                    }
                }
                selectedCategory = categoryIndex;
                viewFlipper.setDisplayedChild(categoryIndex);
            }
        };
        icon.setOnClickListener(listener);
        text.setOnClickListener(listener);
        icon.setTag("icon_" + index);
        text.setTag("text_" + index);

        item.addView(icon);
        item.addView(text);
        return item;
    }

    private LinearLayout createModulePanel() {
        LinearLayout panel = new LinearLayout(context);
        panel.setBackground(bgContent);
        panel.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams panelParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        panel.setLayoutParams(panelParams);

        viewFlipper = new ViewFlipper(context);
        viewFlipper.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        viewFlipper.setBackground(bgContent);

        categoryLayouts = new LinearLayout[titles.length];
        for (int i = 0; i < titles.length; i++) {
            categoryLayouts[i] = new LinearLayout(context);
            categoryLayouts[i].setOrientation(LinearLayout.VERTICAL);
            categoryLayouts[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            categoryLayouts[i].setBackground(bgContent);
            viewFlipper.addView(categoryLayouts[i]);
        }

        controllerModule.loadModule(context, rootView);


        ControllerModule.ModuleData killaura = new ControllerModule.ModuleData("Killaura", killauraEnabled, true);
        killaura.addSlider(1, "CP" + "S", killauraC, 1, 20);
        killaura.addSlider(2, "CPS", killauraD,1.0f, 16.0f);
        controllerModule.addModule(categoryLayouts[0], killaura);


        ScrollView scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        scrollView.setBackground(bgContent);
        scrollView.setPadding(8,8,8,8);
        scrollView.addView(viewFlipper);

        panel.addView(scrollView);
        viewFlipper.setDisplayedChild(indexs);


        return panel;
    }

    private View createSpacer(int height) {
        View spacer = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip(height));
        spacer.setLayoutParams(params);
        return spacer;
    }

    private void showPopup(LinearLayout content) {
        PopupWindow popup = new PopupWindow(content, dip(330), LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popup.setAnimationStyle(1);
        popup.showAtLocation(rootView, Gravity.CENTER, 0, 0);
    }

    private int dip(int value) {
        return utils.dips(context, value);
    }

    private int sp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().scaledDensity);
    }

    void toast(String text){
        utils.toast(context, text);
    }
    public LinearLayout getCombatLayout() { return categoryLayouts[0]; }
    public LinearLayout getMovementLayout() { return categoryLayouts[1]; }
    public LinearLayout getVisualLayout() { return categoryLayouts[2]; }
    public LinearLayout getOtherLayout() { return categoryLayouts[3]; }
    public LinearLayout getConfigLayout() { return categoryLayouts[4]; }
}