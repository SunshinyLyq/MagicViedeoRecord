package lyq.com.magicvideorecord.camera.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import lyq.com.magicvideorecord.R;

/**
 * @author sunshiny
 * @date 2018/12/22.
 * @desc
 */
public class FocusImageView extends android.support.v7.widget.AppCompatImageView {
    private static final String TAG = "FocusImageView";
    private static final int NO_ID = -1;
    private int mFocusImg = NO_ID;
    private int mFocusSucceedImg = NO_ID;
    private int mFocusFailedImg = NO_ID;
    private Animation mAnimation;
    private Handler mHandler;

    public FocusImageView(Context context) {
        super(context, null);
    }

    public FocusImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.focusview_show);
        setVisibility(GONE);
        mHandler = new Handler();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FocusImageView);
        mFocusImg = a.getResourceId(R.styleable.FocusImageView_focus_focusing_id, NO_ID);
        mFocusSucceedImg = a.getResourceId(R.styleable.FocusImageView_focus_success_id, NO_ID);
        mFocusFailedImg = a.getResourceId(R.styleable.FocusImageView_focus_fail_id, NO_ID);
        a.recycle();
    }

    /**
     * 开始聚焦
     */
    public void startFocus(Point point) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        layoutParams.topMargin = point.y - getHeight() / 2;
        layoutParams.leftMargin = point.x - getWidth() / 2;
        setLayoutParams(layoutParams);
        setVisibility(VISIBLE);
        setImageResource(mFocusImg);
        startAnimation(mAnimation);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(GONE);
            }
        }, 3500);

    }

    /**
     * 聚焦成功
     */
    public void onFocusSuccess() {
        setImageResource(mFocusSucceedImg);
        mHandler.removeCallbacks(null, null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(GONE);
            }
        }, 1000);
    }

    public void onFocusFailed() {
        setImageResource(mFocusFailedImg);
        mHandler.removeCallbacks(null, null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(GONE);
            }
        }, 1000);
    }


    public void setFocusImg(int mFocusImg) {
        this.mFocusImg = mFocusImg;
    }

    public void setmFocusSucceedImg(int mFocusSucceedImg) {
        this.mFocusSucceedImg = mFocusSucceedImg;
    }
}
