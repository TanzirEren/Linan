package com.linan.colorconverter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/** HSV color wheel: angle = hue, distance from center = saturation. */
public class ColorWheelView extends View {

    public interface Listener {
        void onChanged();
    }

    private final Paint wheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint satPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float cx, cy, radius;
    private float hue = 0f, sat = 0f, val = 1f;
    private Listener listener;

    public ColorWheelView(Context context) {
        super(context);
        init();
    }

    public ColorWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(Ui.dp(getContext(), 3));
        ringPaint.setColor(Color.WHITE);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(Ui.dp(getContext(), 1));
        outlinePaint.setColor(0x66000000);
    }

    public void setListener(Listener l) {
        listener = l;
    }

    public void setHsv(float h, float s) {
        hue = h;
        sat = s;
        invalidate();
    }

    public void setValue(float v) {
        val = v;
        invalidate();
    }

    /** Opaque RGB color at the current selection. */
    public int getColor() {
        return Color.HSVToColor(new float[]{hue, sat, val});
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        float pad = Ui.dp(getContext(), 16);
        cx = w / 2f;
        cy = h / 2f;
        radius = Math.min(w, h) / 2f - pad;
        int[] colors = new int[]{0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF,
                0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};
        wheelPaint.setShader(new SweepGradient(cx, cy, colors, null));
        satPaint.setShader(new RadialGradient(cx, cy, radius, 0xFFFFFFFF, 0x00FFFFFF,
                Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(cx, cy, radius, wheelPaint);
        canvas.drawCircle(cx, cy, radius, satPaint);
        dimPaint.setColor(Color.argb(Math.round((1f - val) * 255f), 0, 0, 0));
        canvas.drawCircle(cx, cy, radius, dimPaint);

        double rad = Math.toRadians(hue);
        float sx = cx + (float) Math.cos(rad) * sat * radius;
        float sy = cy + (float) Math.sin(rad) * sat * radius;
        fillPaint.setColor(getColor());
        float r = Ui.dp(getContext(), 12);
        canvas.drawCircle(sx, sy, r, fillPaint);
        canvas.drawCircle(sx, sy, r, ringPaint);
        canvas.drawCircle(sx, sy, r + Ui.dp(getContext(), 1.5f), outlinePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int a = e.getActionMasked();
        if (a == MotionEvent.ACTION_DOWN || a == MotionEvent.ACTION_MOVE) {
            if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
            float dx = e.getX() - cx;
            float dy = e.getY() - cy;
            double ang = Math.toDegrees(Math.atan2(dy, dx));
            if (ang < 0) ang += 360;
            hue = (float) ang;
            sat = Math.min(1f, (float) Math.sqrt(dx * dx + dy * dy) / radius);
            invalidate();
            if (listener != null) listener.onChanged();
            return true;
        }
        return super.onTouchEvent(e);
    }
}
