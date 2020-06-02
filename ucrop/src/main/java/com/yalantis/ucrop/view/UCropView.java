package com.yalantis.ucrop.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.yalantis.ucrop.R;
import com.yalantis.ucrop.callback.CropBoundsChangeListener;
import com.yalantis.ucrop.callback.OverlayViewChangeListener;

import androidx.annotation.NonNull;

public class UCropView extends FrameLayout {

    private GestureCropImageView mGestureCropImageView;
    private final OverlayView mViewOverlay;
    private final View mCoverTextOverlay;
    private final TextView mCoverTextView;

    public UCropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.ucrop_view, this, true);
        mGestureCropImageView = findViewById(R.id.image_view_crop);
        mViewOverlay = findViewById(R.id.view_overlay);
        mCoverTextOverlay = findViewById(R.id.cover_text_overlay);
        mCoverTextView = findViewById(R.id.cover_text_view);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ucrop_UCropView);
        mViewOverlay.processStyledAttributes(a);
        mGestureCropImageView.processStyledAttributes(a);
        a.recycle();

        mCoverTextOverlay.setVisibility(View.GONE);

        setListenersToViews();

        setCoverTextOverlayPosition(mViewOverlay.getCropViewRect());
    }

    private void setListenersToViews() {
        mGestureCropImageView.setCropBoundsChangeListener(new CropBoundsChangeListener() {
            @Override
            public void onCropAspectRatioChanged(float cropRatio) {
                mViewOverlay.setTargetAspectRatio(cropRatio);
            }
        });
        mViewOverlay.setOverlayViewChangeListener(new OverlayViewChangeListener() {
            @Override
            public void onCropRectUpdated(RectF cropRect) {
                mGestureCropImageView.setCropRect(cropRect);
                setCoverTextOverlayPosition(cropRect);
            }
        });
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @NonNull
    public GestureCropImageView getCropImageView() {
        return mGestureCropImageView;
    }

    @NonNull
    public OverlayView getOverlayView() {
        return mViewOverlay;
    }

    public void setCoverText(CharSequence coverText) {
        mCoverTextOverlay.setVisibility(View.VISIBLE);
        mCoverTextView.setText(coverText);
    }

    /**
     * Method for reset state for UCropImageView such as rotation, scale, translation.
     * Be careful: this method recreate UCropImageView instance and reattach it to layout.
     */
    public void resetCropImageView() {
        removeView(mGestureCropImageView);
        mGestureCropImageView = new GestureCropImageView(getContext());
        setListenersToViews();
        mGestureCropImageView.setCropRect(getOverlayView().getCropViewRect());
        addView(mGestureCropImageView, 0);
    }

    private void setCoverTextOverlayPosition(final RectF cropRect) {
        Runnable changeParams = new Runnable() {
            @Override
            public void run() {
                LayoutParams layoutParams = new LayoutParams(Math.round(cropRect.width()), Math.round(cropRect.height()));
                layoutParams.topMargin = Math.round(cropRect.top);
                layoutParams.leftMargin = Math.round(cropRect.left);
                mCoverTextOverlay.setLayoutParams(layoutParams);
                mCoverTextOverlay.requestLayout();

                float textSize = (cropRect.height() * 2f / 16f) / 2.9f;
                mCoverTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }
        };
        this.post(changeParams);
    }
}