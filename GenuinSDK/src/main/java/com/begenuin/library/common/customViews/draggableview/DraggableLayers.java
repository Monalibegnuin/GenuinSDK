package com.begenuin.library.common.customViews.draggableview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import com.begenuin.begenuin.ui.customview.draggableview.DraggableBaseCustomView;
import com.begenuin.library.core.enums.LayerType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DraggableLayers extends View {
    public List<Layer> layers = new LinkedList<Layer>();
    public IDraggableLayerInterface draggableLayerInterface;
    public int videoContainerLeft, videoContainerTop;
    public float widthFactor, heightFactor;

    public DraggableLayers(Context context) {
        super(context);
        init(context);
    }

    public DraggableLayers(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DraggableLayers(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        this.setLayoutParams(layoutParams);
    }

    public void setListener(IDraggableLayerInterface draggableLayerInterface) {
        this.draggableLayerInterface = draggableLayerInterface;
    }

    public void addLayer(Activity context, Bitmap b, int viewId, DraggableBaseCustomView draggableTextView) {
        Layer l = new Layer(context, this, b, viewId, draggableTextView, videoContainerLeft, videoContainerTop, LayerType.IMAGE);
        layers.add(l);
    }

    public void addLayer(Activity context, Bitmap b, int viewId, DraggableBaseCustomView draggableTextView, LayerType layerType) {
        Layer l = new Layer(context, this, b, viewId, draggableTextView, videoContainerLeft, videoContainerTop, layerType);
        layers.add(l);
    }

    public void addLayer(Activity context, ArrayList<Bitmap> b, ArrayList<Integer> mDelays, int viewId, DraggableBaseCustomView draggableTextView) {
        Layer l = new Layer(context, this, b, mDelays, viewId, draggableTextView, videoContainerLeft, videoContainerTop);
        layers.add(l);
    }

    public void removeLayer(Layer layer) {
        layers.remove(layer);
        invalidate();
    }

    public void setValues(int videoContainerLeft, int videoContainerTop, float widthFactor, float heightFactor) {
        this.videoContainerLeft = videoContainerLeft;
        this.videoContainerTop = videoContainerTop;
        this.widthFactor = widthFactor;
        this.heightFactor = heightFactor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Layer l : layers) {
            l.draw(canvas);
        }
    }

    private Layer target;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            target = null;
            for (int i = layers.size() - 1; i >= 0; i--) {
                Layer l = layers.get(i);
                if (l.contains(event)) {
                    target = l;
                    if(l.layerType != LayerType.FULL_IMAGE) {
                        layers.remove(l);
                        layers.add(l);
                    }
                    invalidate();
                    break;
                }
            }
        }
        if (target == null) {
            return false;
        }
        return target.onTouchEvent(event);
    }
}
