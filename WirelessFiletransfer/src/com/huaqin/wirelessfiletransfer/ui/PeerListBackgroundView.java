package com.huaqin.wirelessfiletransfer.ui;


import com.huaqin.wirelessfiletransfer.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PeerListBackgroundView extends View {
    private int height = 0;
    private int width = 0;
    public static int smallradius = 0;
    public static int largeradius = 0;
    public static int[] circlex = new int[8];
    public static int[] circley = new int[8];
    public static int[] radius = new int[6];
    public static boolean isStarted;
    private Paint p;
    private Context ctx;

    public PeerListBackgroundView(Context context) {
        super(context);
        p = new Paint();
        ctx = context;
    }

    public PeerListBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        p = new Paint();
        ctx = context;
    }

    public PeerListBackgroundView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        p = new Paint();
        ctx = context;
    }

    protected void caculateRadiusAndCirclexy(int height, int width) {
        radius[0] = width / 16;
        radius[1] = width / 10;
        radius[2] = width / 5;

        radius[3] = width / 3;
        radius[4] = width / 2 + 20;
        radius[5] = width / 2 + width / 4;

        circlex[0] = width / 2 + radius[1];
        circley[0] = height / 2;

        circlex[1] = (int) (width / 2 + radius[3] * Math.cos(1.4));
        circley[1] = (int) (height / 2 + radius[3] * Math.sin(1.4));

        circlex[2] = (int) (width / 2 - radius[4] * Math.cos(0.7));
        circley[2] = (int) (height / 2 + radius[4] * Math.sin(0.7));

        circlex[3] = (int) (width / 2 - radius[4] * Math.cos(1.04));
        circley[3] = (int) (height / 2 - radius[4] * Math.sin(1.04));

        circlex[4] = (int) (width / 2 + radius[4] * Math.cos(0.78));
        circley[4] = (int) (height / 2 - radius[4] * Math.sin(0.78));

        circlex[5] = (int) (width / 2 + radius[4] * Math.cos(0.9));
        circley[5] = (int) (height / 2 + radius[4] * Math.sin(0.9));

        circlex[6] = (int) (width / 2 - radius[3] * Math.cos(0.1));
        circley[6] = (int) (height / 2 + radius[3] * Math.sin(0.1));

        circlex[7] = (int) (width / 2 + radius[3] * Math.cos(1.4));
        circley[7] = (int) (height / 2 - radius[3] * Math.sin(1.4));

        smallradius = width / 45;
        largeradius = width / 30;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        height = canvas.getHeight();
        width = canvas.getWidth();
        caculateRadiusAndCirclexy(height, width);

        p.setColor(ctx.getResources().getColor(R.color.radarcircle));
        p.setStyle(Paint.Style.STROKE);
        p.setTextSize(30);
        p.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
        canvas.drawCircle(width / 2, height / 2, radius[0], p);
        canvas.drawCircle(width / 2, height / 2, radius[1], p);
        canvas.drawCircle(width / 2, height / 2, radius[2], p);
        canvas.drawCircle(width / 2, height / 2, radius[3], p);
        canvas.drawCircle(width / 2, height / 2, radius[4], p);
        canvas.drawCircle(width / 2, height / 2, radius[5], p);
        isStarted = true;

    }

}

