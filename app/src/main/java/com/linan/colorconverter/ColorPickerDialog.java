package com.linan.colorconverter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/** Color wheel + brightness + opacity dialog. */
public final class ColorPickerDialog {

    public interface Callback {
        void onPicked(int argb);
    }

    private ColorPickerDialog() {}

    private abstract static class SimpleSeek implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

    private static TextView label(Activity a, String text) {
        TextView t = new TextView(a);
        t.setText(text);
        t.setTextSize(11);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(0xFF6F7A83);
        t.setPadding(0, Ui.dp(a, 12), 0, 0);
        return t;
    }

    public static void show(final Activity a, int initial, final Callback cb) {
        final ColorWheelView wheel = new ColorWheelView(a);
        final SeekBar sbVal = new SeekBar(a);
        final SeekBar sbAlpha = new SeekBar(a);
        final View swatch = new View(a);
        final TextView tvHex = new TextView(a);
        final int[] current = new int[]{initial};

        sbVal.setMax(100);
        sbAlpha.setMax(255);

        float[] hsv = new float[3];
        Color.colorToHSV(initial, hsv);
        wheel.setHsv(hsv[0], hsv[1]);
        wheel.setValue(hsv[2]);
        sbVal.setProgress(Math.round(hsv[2] * 100f));
        sbAlpha.setProgress(Color.alpha(initial));

        final Runnable update = new Runnable() {
            @Override
            public void run() {
                int rgb = wheel.getColor() & 0xFFFFFF;
                current[0] = (sbAlpha.getProgress() << 24) | rgb;
                swatch.setBackground(Ui.swatch(a, current[0], 14));
                tvHex.setText(SmaliUtils.toAndroidHex(current[0]) + "\n"
                        + SmaliUtils.toSmaliValue(current[0]));
            }
        };

        wheel.setListener(new ColorWheelView.Listener() {
            @Override
            public void onChanged() {
                update.run();
            }
        });
        sbVal.setOnSeekBarChangeListener(new SimpleSeek() {
            @Override
            public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                wheel.setValue(p / 100f);
                update.run();
            }
        });
        sbAlpha.setOnSeekBarChangeListener(new SimpleSeek() {
            @Override
            public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                update.run();
            }
        });

        LinearLayout root = new LinearLayout(a);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(a, 20);
        root.setPadding(pad, Ui.dp(a, 12), pad, 0);

        root.addView(wheel, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(a, 270)));
        root.addView(label(a, "BRIGHTNESS"));
        root.addView(sbVal);
        root.addView(label(a, "OPACITY"));
        root.addView(sbAlpha);

        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, Ui.dp(a, 14), 0, 0);
        row.addView(swatch, new LinearLayout.LayoutParams(Ui.dp(a, 56), Ui.dp(a, 56)));
        tvHex.setTypeface(Typeface.MONOSPACE);
        tvHex.setTextSize(15);
        tvHex.setTextColor(0xFF1F2A33);
        tvHex.setPadding(Ui.dp(a, 14), 0, 0, 0);
        row.addView(tvHex);
        root.addView(row);

        update.run();

        new AlertDialog.Builder(a)
                .setTitle("Choose a color")
                .setView(root)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("SELECT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        cb.onPicked(current[0]);
                    }
                })
                .show();
    }
}
