package com.linan.colorconverter;

import android.graphics.Color;

import java.util.Locale;

/** Color conversion helpers (hex <-> Smali). All colors are ARGB ints. */
public final class SmaliUtils {

    private SmaliUtils() {}

    /** Parses RGB, ARGB, RRGGBB or AARRGGBB (with or without #). */
    public static int parseHex(String input) throws NumberFormatException {
        if (input == null) throw new NumberFormatException("empty");
        String s = input.trim();
        if (s.startsWith("#")) s = s.substring(1);
        if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
        int n = s.length();
        for (int i = 0; i < n; i++) {
            if (Character.digit(s.charAt(i), 16) < 0) throw new NumberFormatException("bad char");
        }
        if (n == 3 || n == 4) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                sb.append(s.charAt(i)).append(s.charAt(i));
            }
            s = sb.toString();
            n = s.length();
        }
        if (n != 6 && n != 8) throw new NumberFormatException("bad length");
        long v = Long.parseLong(s, 16);
        if (n == 6) v |= 0xFF000000L;
        return (int) v;
    }

    /** Parses 0xAARRGGBB, -0x191a1a, decimal or a whole "const v0, ..." line. */
    public static int parseSmali(String input) throws NumberFormatException {
        if (input == null) throw new NumberFormatException("empty");
        String s = input.trim();
        if (s.startsWith("#") && s.length() > 1 && Character.digit(s.charAt(1), 16) >= 0) {
            return parseHex(s);
        }
        int hash = s.indexOf('#');
        if (hash >= 0) s = s.substring(0, hash).trim();
        int comma = s.lastIndexOf(',');
        if (comma >= 0) s = s.substring(comma + 1).trim();
        if (s.length() == 0) throw new NumberFormatException("empty");
        s = s.split("\\s+")[0];
        boolean neg = false;
        if (s.startsWith("-")) {
            neg = true;
            s = s.substring(1);
        }
        long v;
        if (s.startsWith("0x") || s.startsWith("0X")) {
            v = Long.parseLong(s.substring(2), 16);
        } else {
            v = Long.parseLong(s);
        }
        if (neg) v = -v;
        if (v < -2147483648L || v > 0xFFFFFFFFL) throw new NumberFormatException("range");
        return (int) v;
    }

    /** 0xaarrggbb */
    public static String toSmaliValue(int argb) {
        return "0x" + String.format(Locale.US, "%08x", argb);
    }

    /** baksmali style signed value, e.g. -0x191a1a */
    public static String toSignedSmali(int argb) {
        if (argb < 0) return "-0x" + Long.toHexString(-(long) argb);
        return "0x" + Integer.toHexString(argb);
    }

    /** const / const/high16 line */
    public static String toConstLine(int argb, String reg) {
        if ((argb & 0xFFFF) == 0) {
            return "const/high16 " + reg + ", " + toSmaliValue(argb);
        }
        return "const " + reg + ", " + toSmaliValue(argb);
    }

    /** #RRGGBB when opaque, #AARRGGBB otherwise. */
    public static String toAndroidHex(int argb) {
        if (Color.alpha(argb) == 255) {
            return String.format(Locale.US, "#%06X", argb & 0xFFFFFF);
        }
        return String.format(Locale.US, "#%08X", argb);
    }

    public static String toFullHex(int argb) {
        return String.format(Locale.US, "#%08X", argb);
    }

    public static String toRgbString(int argb) {
        if (Color.alpha(argb) == 255) {
            return "rgb(" + Color.red(argb) + ", " + Color.green(argb) + ", " + Color.blue(argb) + ")";
        }
        return "rgba(" + Color.red(argb) + ", " + Color.green(argb) + ", " + Color.blue(argb)
                + ", " + String.format(Locale.US, "%.2f", Color.alpha(argb) / 255f) + ")";
    }

    public static String toHsvString(int argb) {
        float[] hsv = new float[3];
        Color.colorToHSV(argb, hsv);
        return String.format(Locale.US, "hsv(%d, %d%%, %d%%)",
                Math.round(hsv[0]), Math.round(hsv[1] * 100), Math.round(hsv[2] * 100));
    }

    public static int alphaPercent(int argb) {
        return Math.round(Color.alpha(argb) / 255f * 100f);
    }

    /** Mixes RGB toward target by t (0..1), keeps alpha. */
    public static int mix(int argb, int target, float t) {
        int r = Math.round(Color.red(argb) + (Color.red(target) - Color.red(argb)) * t);
        int g = Math.round(Color.green(argb) + (Color.green(target) - Color.green(argb)) * t);
        int b = Math.round(Color.blue(argb) + (Color.blue(target) - Color.blue(argb)) * t);
        return Color.argb(Color.alpha(argb), r, g, b);
    }
}
