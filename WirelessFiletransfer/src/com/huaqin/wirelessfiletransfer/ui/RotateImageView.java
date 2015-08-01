package com.huaqin.wirelessfiletransfer.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RotateImageView extends ImageView implements Runnable {

    private float rotatedegree = 0.0f;
    private Drawable draw = null;
    private Thread mThread;
    private boolean rotateState = false;

    public RotateImageView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public RotateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public RotateImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Drawable drawable = getDrawable();
        if (drawable == null)
            return;

        
        Rect bounds = drawable.getBounds();
        int w = bounds.right - bounds.left;
        int h = bounds.bottom - bounds.top;

        if (w == 0 || h == 0)
            return; // nothing to draw

        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();
        int width = getWidth() - left - right;
        int height = getHeight() - top - bottom;

        int saveCount = canvas.getSaveCount();

        // Scale down the image first if required.
        if ((getScaleType() == ImageView.ScaleType.FIT_CENTER)
                && ((width < w) || (height < h))) {
            float ratio = Math.min((float) width / w, (float) height / h);
            canvas.scale(ratio, ratio, width / 2.0f, height / 2.0f);
        }
        if ((getScaleType() == ImageView.ScaleType.CENTER_CROP)
                && ((width < w) || (height < h))) {
            float ratio = Math.max((float) width / w, (float) height / h);
            canvas.scale(ratio, ratio , width / 2.0f, height / 2.0f);
        }
        canvas.translate(left + width / 2, top + height / 2);
        canvas.rotate(rotatedegree);
        canvas.translate(-w / 2, -h / 2);
        drawable.draw(canvas);
        canvas.restoreToCount(saveCount);

        //super.onDraw(canvas);
    }

    public void setDegree(float rotatedegree) {
        this.rotatedegree = rotatedegree;
        this.invalidate();
    }

    public void startRotate() {
        rotateState = true;
        mThread = new Thread(this);
        mThread.start();
    }
 
    public void stopRotate() {
        rotateState = false;

    }

    public void reStartRotate() {
        rotateState = true;
    }

    public void exitView() {
        mThread.interrupt();
        mThread.stop();
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        while (rotateState) {

            rotatedegree--;
            this.postInvalidate();
            try {
                Thread.sleep(50);
            }
            catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

}

