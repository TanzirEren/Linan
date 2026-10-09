package com.linan.colorconverter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

/** Small drawing helpers. */
public final class Ui {

    private Ui() {}

    public static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    /** Rounded swatch with a soft outline so light colors stay visible. */
    public static GradientDrawable swatch(Context c, int argb, float radiusDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setCornerRadius(dp(c, radiusDp));
        gd.setColor(argb);
        gd.setStroke(dp(c, 1.5f), Color.parseColor("#D9D4B8"));
        return gd;
    }

    /** Rainbow circle used as the color-chooser button. */
    public static GradientDrawable rainbow(Context c) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setGradientType(GradientDrawable.SWEEP_GRADIENT);
        gd.setColors(new int[]{
                0xFFFF4D4D, 0xFFFFD84D, 0xFF7CE04D, 0xFF4DE0D0,
                0xFF4D7CFF, 0xFFD04DFF, 0xFFFF4D4D});
        gd.setStroke(dp(c, 3), Color.WHITE);
        return gd;
    }
}
