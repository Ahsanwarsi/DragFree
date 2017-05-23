package com.bufferlogics.dragfree.gestures;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ahsanwarsi on 16/10/15.
 * Copyright (c) 2015 DeviceBee. All rights reserved.
 */

public class DragView extends View {
    List<Layer> layers = new LinkedList<Layer>();
    public boolean isEnabled = true;
    public float zoomScale = 1;
    //int[] ids = {R.drawable.ab, R.drawable.ab, R.drawable.ab};
    private Context context;
    //private Canvas mCanvas;

    public DragView(Context context) {
        super(context);
        this.context = context;
    }

    public DragView(Context context, Bitmap bitmap, OnClickPressedWithTouch onClick) {
        super(context);
        //Resources res = getResources();
        Layer l = new Layer(context, this, bitmap, onClick);
        layers.add(l);
    }

    public void addView(Bitmap bitmap, OnClickPressedWithTouch onClick) {
        Layer l = new Layer(context, this, bitmap, onClick);
        layers.add(l);
    }

    public void setImage(Context context, Bitmap bitmap, OnClickPressedWithTouch onClick) {
        Layer l = new Layer(context, this, bitmap, onClick);
        layers.add(l);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Layer l : layers) {
            l.draw(canvas);
        }
    }

    public void zoomIn() {
        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer l = layers.get(i);
            target = l;
            layers.remove(l);
            l.zoomIn();
            layers.add(l);
            invalidate();
        }

    }

    public void zoomOut() {
        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer l = layers.get(i);
            target = l;
            layers.remove(l);
            l.zoomOut();
            layers.add(l);
            invalidate();
        }

    }

    private Layer target;

    @SuppressWarnings("unused")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                target = null;
                for (int i = layers.size() - 1; i >= 0; i--) {
                    Layer l = layers.get(i);
                    target = l;
                    layers.remove(l);
                    layers.add(l);
                    invalidate();
                    break;
                }
            }

            if (target == null) {
                return false;
            }
            try {
                return target.onTouchEvent(event);
            } catch (Exception e) {
                return false;
            }

        } else
            return false;
    }

    public interface OnClickPressedWithTouch
    {
        public void onClick(View v);
        public void onDoubleTab(View v);
    }

}

class Layer {
    Matrix matrix = new Matrix();
    Matrix inverse = new Matrix();
    RectF bounds;
    View parent;
    Bitmap bitmap;
    MoveGestureDetector mgd;
    ScaleGestureDetector sgd;
    RotateGestureDetector rgd;
    GestureDetector gest;
    private float zoomScale = 1;
    private DragView.OnClickPressedWithTouch onClick;

    public Layer(Context ctx, final View p, final Bitmap b, DragView.OnClickPressedWithTouch onClick) {
        parent = p;
        bitmap = b;
        bounds = new RectF(0, 0, b.getWidth(), b.getHeight());
        mgd = new MoveGestureDetector(ctx, mgl);
        sgd = new ScaleGestureDetector(ctx, sgl); //ScaleGestureDetector
        rgd = new RotateGestureDetector(ctx, rgl);
        gest = new GestureDetector(ctx, gestureListener);
        this.onClick = onClick;

        p.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                matrix.postTranslate((p.getWidth() - bitmap.getWidth()) / 2, (p.getHeight() - bitmap.getHeight()) / 2);
                p.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        //matrix.postTranslate(50 + (float) Math.random() * 50, 50 + (float) Math.random() * 50);

    }

    public boolean contains(MotionEvent event) {
        matrix.invert(inverse);
        float[] pts = {event.getX(), event.getY()};
        inverse.mapPoints(pts);
        if (!bounds.contains(pts[0], pts[1])) {
            return false;
        }
        return Color.alpha(bitmap.getPixel((int) pts[0], (int) pts[1])) != 0;

    }

    public boolean onTouchEvent(MotionEvent event) {
        if (contains(event)) {
            mgd.onTouchEvent(event);
            sgd.onTouchEvent(event);
            rgd.onTouchEvent(event);
            gest.onTouchEvent(event);
            return true;
        }
        return false;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, matrix, null);
    }

    public void zoomIn() {
        zoomScale = (float) (zoomScale + 0.1);
        matrix.postScale(zoomScale, zoomScale);
        parent.invalidate();
    }

    public void zoomOut() {
        zoomScale = (float) (zoomScale - 0.1);
        matrix.postScale(1 / zoomScale, 1 / zoomScale);
        parent.invalidate();
    }

    MoveGestureDetector.SimpleOnMoveGestureListener mgl = new MoveGestureDetector.SimpleOnMoveGestureListener() {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            PointF delta = detector.getFocusDelta();
            matrix.postTranslate(delta.x, delta.y);
            parent.invalidate();
            return true;
        }
    };

    SimpleOnScaleGestureListener sgl = new SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            matrix.postScale(scale, scale, detector.getFocusX(), detector.getFocusY());
            parent.invalidate();
            return true;
        }
    };

    RotateGestureDetector.SimpleOnRotateGestureListener rgl = new RotateGestureDetector.SimpleOnRotateGestureListener() {
        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            matrix.postRotate(-detector.getRotationDegreesDelta(), detector.getFocusX(), detector.getFocusY());
            parent.invalidate();
            return true;
        }
    };

    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            onClick.onClick(parent);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            onClick.onDoubleTab(parent);
            return true;
        }
    };
}