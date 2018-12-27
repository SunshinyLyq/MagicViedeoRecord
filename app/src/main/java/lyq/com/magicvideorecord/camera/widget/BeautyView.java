package lyq.com.magicvideorecord.camera.widget;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import lyq.com.magicvideorecord.R;

/**
 * @author sunshiny
 * @date 2018/12/27.
 * @desc
 */
public class BeautyView extends LinearLayout {


    private SeekBar mBuffing;
    private SeekBar mFaceThin;
    private SeekBar mBigEye;

    private TextView tv_buffing_progress;
    private TextView tv_face_progress;
    private TextView tv_eye_progress;

    private onBeautyChangeCallback mCallback;

    public BeautyView(@NonNull Context context) {
        super(context);
    }

    public BeautyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BeautyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCallback(onBeautyChangeCallback callback) {
        this.mCallback = callback;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mBuffing = findViewById(R.id.buffing_seek_bar);
        mFaceThin = findViewById(R.id.face_thin_seek_bar);
        mBigEye = findViewById(R.id.big_eye_seek_bar);

        tv_buffing_progress = findViewById(R.id.tv_buffing_progress);
        tv_face_progress = findViewById(R.id.tv_face_progress);
        tv_eye_progress = findViewById(R.id.tv_eye_progress);

        mBuffing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_buffing_progress.setText("" + progress);

                int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                tv_buffing_progress.measure(spec, spec);
                int quotaWidth = tv_buffing_progress.getMeasuredWidth();

                int spec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                tv_buffing_progress.measure(spec2, spec2);
                int sbWidth = mBuffing.getMeasuredWidth();
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv_buffing_progress.getLayoutParams();
                params.leftMargin = (int) (((double) progress / mBuffing.getMax()) * sbWidth - (double) quotaWidth * progress / mBuffing.getMax());
                tv_buffing_progress.setLayoutParams(params);


                mBuffing.setProgress(progress);
                mCallback.onBufferChange(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //开始拖动

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //拖动结束
            }
        });

        mFaceThin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                tv_face_progress.setText("" + progress);

                int spec = View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                tv_face_progress.measure(spec, spec);
                int quotaWidth = tv_face_progress.getMeasuredWidth();

                int spec2 = View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                tv_face_progress.measure(spec2, spec2);
                int seekBarWidth = mFaceThin.getMeasuredWidth();

                LinearLayout.LayoutParams params = ((LinearLayout.LayoutParams) tv_face_progress.getLayoutParams());
                params.leftMargin = (int) (((double) progress / mBuffing.getMax()) * seekBarWidth - (double) quotaWidth * progress / mBuffing.getMax());
                tv_face_progress.setLayoutParams(params);


                mFaceThin.setProgress(progress);
                mCallback.onFaceThinChange(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBigEye.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                tv_eye_progress.setText("" + progress);

                int spec = View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                tv_eye_progress.measure(spec, spec);
                int quotaWidth =  tv_eye_progress.getMeasuredWidth();

                int spec2 = View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                tv_eye_progress.measure(spec2, spec2);
                int seekBarWidth = mBigEye.getMeasuredWidth();

                LinearLayout.LayoutParams params = ((LinearLayout.LayoutParams)  tv_eye_progress.getLayoutParams());
                params.leftMargin = (int) (((double) progress / mBigEye.getMax()) * seekBarWidth - (double) quotaWidth * progress / mBigEye.getMax());
                tv_eye_progress.setLayoutParams(params);


                mBigEye.setProgress(progress);
                mCallback.onBigEyeChange(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void show() {
        int height = getResources().getDimensionPixelSize(R.dimen.filter_height);
        setTranslationY(height);
        setAlpha(0);
        setVisibility(View.VISIBLE);
        animate().setDuration(400).alpha(1f).translationY(0).setListener(null).start();
    }

    public void hide() {
        animate().setDuration(400).alpha(0f).translationY(getHeight()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }


    public interface onBeautyChangeCallback {
        void onBufferChange(int ration);

        void onFaceThinChange(int ration);

        void onBigEyeChange(int ratio);
    }
}
