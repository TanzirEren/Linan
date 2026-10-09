package com.linan.colorconverter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {

    private ScrollView scroll;
    private EditText etHex, etSmali;
    private View btnPick, vPreview, vSmaliPreview, cardResult, llViewResult;
    private TextView tvHexInfo, tvSmali, tvSmaliInfo, tvEmpty;
    private LinearLayout llFormats, llShades, llHistory;

    private int currentArgb = 0xFFE6E6E6;
    private int smaliViewArgb = 0xFFE6E6E6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupSystemBars();
        bindViews();
        setupListeners();
        refreshHistory();
    }

    private void setupSystemBars() {
        if (Build.VERSION.SDK_INT >= 23) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void bindViews() {
        scroll = (ScrollView) findViewById(R.id.scroll);
        etHex = (EditText) findViewById(R.id.etHex);
        etSmali = (EditText) findViewById(R.id.etSmali);
        btnPick = findViewById(R.id.btnPick);
        vPreview = findViewById(R.id.vPreview);
        vSmaliPreview = findViewById(R.id.vSmaliPreview);
        cardResult = findViewById(R.id.cardResult);
        llViewResult = findViewById(R.id.llViewResult);
        tvHexInfo = (TextView) findViewById(R.id.tvHexInfo);
        tvSmali = (TextView) findViewById(R.id.tvSmali);
        tvSmaliInfo = (TextView) findViewById(R.id.tvSmaliInfo);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        llFormats = (LinearLayout) findViewById(R.id.llFormats);
        llShades = (LinearLayout) findViewById(R.id.llShades);
        llHistory = (LinearLayout) findViewById(R.id.llHistory);
        btnPick.setBackground(Ui.rainbow(this));
    }

    private void setupListeners() {
        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                int initial = 0xFF5B93C7;
                try {
                    initial = SmaliUtils.parseHex(etHex.getText().toString());
                } catch (NumberFormatException ignored) {
                }
                ColorPickerDialog.show(MainActivity.this, initial, new ColorPickerDialog.Callback() {
                    @Override
                    public void onPicked(int argb) {
                        applyColor(argb, false);
                    }
                });
            }
        });

        findViewById(R.id.btnConvert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertFromInput();
            }
        });

        etHex.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                convertFromInput();
                return true;
            }
        });

        findViewById(R.id.tvPaste).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null && cm.hasPrimaryClip() && cm.getPrimaryClip().getItemCount() > 0) {
                    CharSequence t = cm.getPrimaryClip().getItemAt(0).coerceToText(MainActivity.this);
                    if (t != null) {
                        etHex.setText(t.toString().trim());
                        etHex.setSelection(etHex.getText().length());
                        convertFromInput();
                        return;
                    }
                }
                toast("Clipboard is empty");
            }
        });

        findViewById(R.id.btnCopy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy("Smali color", SmaliUtils.toSmaliValue(currentArgb));
            }
        });

        findViewById(R.id.btnCopyConst).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy("Smali const", SmaliUtils.toConstLine(currentArgb, "v0"));
            }
        });

        findViewById(R.id.btnShare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, SmaliUtils.toAndroidHex(currentArgb) + " -> "
                        + SmaliUtils.toConstLine(currentArgb, "v0"));
                startActivity(Intent.createChooser(i, "Share Smali color"));
            }
        });

        findViewById(R.id.btnView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewSmaliColor();
            }
        });

        etSmali.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                viewSmaliColor();
                return true;
            }
        });

        findViewById(R.id.btnUse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyColor(smaliViewArgb, true);
            }
        });

        findViewById(R.id.tvClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryStore.clear(MainActivity.this);
                refreshHistory();
            }
        });
    }

    // ---------------------------------------------------------------- actions

    private void convertFromInput() {
        hideKeyboard();
        String text = etHex.getText().toString();
        try {
            int argb = SmaliUtils.parseHex(text);
            showResult(argb);
            HistoryStore.add(this, argb);
            refreshHistory();
        } catch (NumberFormatException e) {
            etHex.setError("Enter a valid hex, e.g. #E6E6E6");
        }
    }

    /** Puts a color in the hex box and converts it. */
    private void applyColor(int argb, boolean scrollToResult) {
        etHex.setError(null);
        etHex.setText(SmaliUtils.toAndroidHex(argb));
        etHex.setSelection(etHex.getText().length());
        showResult(argb);
        HistoryStore.add(this, argb);
        refreshHistory();
        if (scrollToResult) scrollToResultCard();
    }

    private void showResult(int argb) {
        currentArgb = argb;
        etHex.setError(null);
        cardResult.setVisibility(View.VISIBLE);
        vPreview.setBackground(Ui.swatch(this, argb, 16));
        tvHexInfo.setText(SmaliUtils.toFullHex(argb) + "\n"
                + SmaliUtils.toRgbString(argb) + "\n"
                + SmaliUtils.toHsvString(argb) + "\n"
                + "alpha " + SmaliUtils.alphaPercent(argb) + "%");
        tvSmali.setText(SmaliUtils.toSmaliValue(argb) + "\n"
                + SmaliUtils.toConstLine(argb, "v0"));
        buildFormats(argb);
        buildShades(argb);
    }

    private void buildFormats(int argb) {
        llFormats.removeAllViews();
        addFormatRow("Smali", SmaliUtils.toSmaliValue(argb));
        addFormatRow("Signed", SmaliUtils.toSignedSmali(argb));
        addFormatRow("Decimal", String.valueOf(argb));
        addFormatRow("Smali const", SmaliUtils.toConstLine(argb, "v0"));
        addFormatRow("Android XML", "<color name=\"my_color\">" + SmaliUtils.toAndroidHex(argb) + "</color>");
        addFormatRow("Java hex", "0x" + String.format(java.util.Locale.US, "%08X", argb));
        addFormatRow("parseColor", "Color.parseColor(\"" + SmaliUtils.toAndroidHex(argb) + "\")");
        addFormatRow("RGB", SmaliUtils.toRgbString(argb));
    }

    private void addFormatRow(String label, final String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_row);
        int h = Ui.dp(this, 12);
        row.setPadding(h, Ui.dp(this, 10), h, Ui.dp(this, 10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = Ui.dp(this, 6);
        row.setLayoutParams(lp);

        TextView tl = new TextView(this);
        tl.setText(label);
        tl.setTextSize(11);
        tl.setTypeface(Typeface.DEFAULT_BOLD);
        tl.setTextColor(getResources().getColor(R.color.muted));
        tl.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 84),
                LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(tl);

        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextSize(13);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setTextColor(getResources().getColor(R.color.ink));
        tv.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tv);

        final String lbl = label;
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy(lbl, value);
            }
        });
        llFormats.addView(row);
    }

    private void buildShades(int argb) {
        llShades.removeAllViews();
        int white = Color.WHITE;
        int black = Color.BLACK;
        int[] shades = new int[]{
                SmaliUtils.mix(argb, white, 0.65f),
                SmaliUtils.mix(argb, white, 0.35f),
                argb,
                SmaliUtils.mix(argb, black, 0.2f),
                SmaliUtils.mix(argb, black, 0.4f),
                SmaliUtils.mix(argb, black, 0.6f)};
        for (int i = 0; i < shades.length; i++) {
            final int c = shades[i];
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            clp.rightMargin = Ui.dp(this, 8);
            cell.setLayoutParams(clp);

            View sw = new View(this);
            sw.setBackground(Ui.swatch(this, c, 14));
            sw.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 54), Ui.dp(this, 54)));
            cell.addView(sw);

            TextView t = new TextView(this);
            t.setText(SmaliUtils.toAndroidHex(c));
            t.setTextSize(9);
            t.setTypeface(Typeface.MONOSPACE);
            t.setTextColor(getResources().getColor(R.color.muted));
            t.setPadding(0, Ui.dp(this, 4), 0, 0);
            cell.addView(t);

            cell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    applyColor(c, false);
                }
            });
            llShades.addView(cell);
        }
    }

    private void viewSmaliColor() {
        hideKeyboard();
        try {
            int argb = SmaliUtils.parseSmali(etSmali.getText().toString());
            smaliViewArgb = argb;
            etSmali.setError(null);
            llViewResult.setVisibility(View.VISIBLE);
            vSmaliPreview.setBackground(Ui.swatch(this, argb, 16));
            tvSmaliInfo.setText(SmaliUtils.toFullHex(argb) + "\n"
                    + SmaliUtils.toRgbString(argb) + "\n"
                    + SmaliUtils.toSignedSmali(argb) + "\n"
                    + "alpha " + SmaliUtils.alphaPercent(argb) + "%");
        } catch (NumberFormatException e) {
            llViewResult.setVisibility(View.GONE);
            etSmali.setError("Use 0xAARRGGBB, -0x... or a const line");
        }
    }

    private void refreshHistory() {
        llHistory.removeAllViews();
        List<Integer> list = HistoryStore.getAll(this);
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        for (int i = 0; i < list.size(); i++) {
            final int c = list.get(i).intValue();
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundResource(R.drawable.bg_row);
            int p = Ui.dp(this, 10);
            row.setPadding(p, p, p, p);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = Ui.dp(this, 6);
            row.setLayoutParams(lp);

            View sw = new View(this);
            sw.setBackground(Ui.swatch(this, c, 10));
            sw.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 36), Ui.dp(this, 36)));
            row.addView(sw);

            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            clp.leftMargin = Ui.dp(this, 12);
            col.setLayoutParams(clp);

            TextView a = new TextView(this);
            a.setText(SmaliUtils.toAndroidHex(c));
            a.setTextSize(14);
            a.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            a.setTextColor(getResources().getColor(R.color.ink));
            col.addView(a);

            TextView b = new TextView(this);
            b.setText(SmaliUtils.toSmaliValue(c));
            b.setTextSize(12);
            b.setTypeface(Typeface.MONOSPACE);
            b.setTextColor(getResources().getColor(R.color.muted));
            col.addView(b);
            row.addView(col);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    applyColor(c, true);
                }
            });
            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    copy("Smali color", SmaliUtils.toSmaliValue(c));
                    return true;
                }
            });
            llHistory.addView(row);
        }
    }

    // ---------------------------------------------------------------- helpers

    private void scrollToResultCard() {
        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.smoothScrollTo(0, Math.max(0, cardResult.getTop() - Ui.dp(MainActivity.this, 8)));
            }
        });
    }

    private void copy(String label, String text) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText(label, text));
            toast("Copied: " + (text.length() > 40 ? text.substring(0, 40) + "…" : text));
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        View f = getCurrentFocus();
        if (f != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(f.getWindowToken(), 0);
        }
    }
}
