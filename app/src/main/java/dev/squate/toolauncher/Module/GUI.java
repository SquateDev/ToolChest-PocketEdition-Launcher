package dev.squate.toolauncher.Module;

import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import dev.squate.toolauncher.R;
import dev.squate.toolauncher.Utils.Utils;


public class GUI {
    float radius = 12f;
    Utils utils = new Utils();
    Context context;
    PopupWindow pop;
    MENU menu = new MENU();

    public void load(Context ctxx){
        context = ctxx;
    }

    public void GUI_open(Context ctx, View parent){
        if(context == null) return;
        int p = 16;
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(90f);
        bg.setColor(Color.parseColor("#043927"));

        LinearLayout lin_root = new LinearLayout(context); // основной линеар
        lin_root.setPadding(p,p,p,p);
        lin_root.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL | Gravity.CENTER_VERTICAL);
        lin_root.setBackground(bg);

        ImageView tool = new ImageView(context);
        tool.setImageResource(R.drawable.tool);
        tool.setScaleType(ImageView.ScaleType.FIT_XY);
        tool.setLayoutParams(new LinearLayout.LayoutParams(dip(25), dip(25)));
        tool.setOnClickListener(view -> {
            menu.loadMenu(context, parent);
        });
        lin_root.addView(tool);


        pop = new PopupWindow(lin_root, dip(45), dip(45));
        pop.showAtLocation(parent, Gravity.TOP | Gravity.LEFT, 50, 50);
    }
    public int dip(int dipx){
        return Math.round(dipx * context.getResources().getDisplayMetrics().density);
    }
}
