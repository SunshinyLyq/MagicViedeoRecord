package lyq.com.magicvideorecord.camera.gpufilter.base;

import android.opengl.GLES20;
import android.view.MotionEvent;
import android.widget.Scroller;

import lyq.com.magicvideorecord.camera.gpufilter.factory.FilterFactory;
import lyq.com.magicvideorecord.camera.gpufilter.factory.FilterType;
import lyq.com.magicvideorecord.config.Constants;
import lyq.com.magicvideorecord.config.MyApplication;
import lyq.com.magicvideorecord.utils.OpenGLUtils;

/**
 * @author sunshiny
 * @date 2018/12/29.
 * @desc 滑动滤镜
 *
 * 基本思路：
 * 当滑动的位置大于手指落下的位置，为右滑，反之为向左滑
 * 当从右向左滑时，在滑动过程中，对当前的画面进行裁剪，
 * 也就是屏幕中呈现出两种滤镜，根据滑动距离来计算出滤镜呈现的大小
 * 这个将滤镜绘制到了FBO中了
 */
public class SlideGpuFilterGroup {

    private FilterType[] filterTypes = new FilterType[]{
            FilterType.NONE,
            FilterType.WARM,
            FilterType.ANTIQUE,
            FilterType.INKWELL,
            FilterType.BRANNAN,
            FilterType.FREUD,
            FilterType.HEFE,
            FilterType.HUDSON,
            FilterType.NASHVILLE,
            FilterType.COOL,
            FilterType.SKETCH
    };

    private GPUImageFilter currentFilter;
    private GPUImageFilter leftFilter;
    private GPUImageFilter rightFilter;

    private int mWidth, mHeight;

    private int[] mFrameBuffers = new int[1];
    private int[] mFrameTextureBuffers = new int[1];

    private int currentIndex = 0;
    private Scroller mScroller;
    private OnFilterChangeListener mListener;

    private int downX;
    private int direction;  //0表示静止，1表示右滑，-1表示左滑
    private int offset;
    private boolean isLocked;
    private boolean isNeedSwitch;

    public SlideGpuFilterGroup() {
        initFilter();
        mScroller = new Scroller(MyApplication.getContext());
    }

    private void initFilter() {
        currentFilter = getFilter(getCurrentIndex());
        leftFilter = getFilter(getLeftIndex());
        rightFilter = getFilter(getRightIndex());
    }

    private GPUImageFilter getFilter(int index) {
        GPUImageFilter filter = FilterFactory.initFilters(filterTypes[index]);

        if (filter == null) {
            filter = new GPUImageFilter();
        }
        return filter;
    }

    public void init() {
        currentFilter.init();
        leftFilter.init();
        rightFilter.init();
    }

    public void onSizeChanged(int width, int height) {
        mWidth = width;
        mHeight = height;

        GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
        OpenGLUtils.genTexturesWithParameter(1, mFrameTextureBuffers, 0, GLES20.GL_RGBA
                , width, height);
        onFilterSizeChanged(width, height);


    }

    private void onFilterSizeChanged(int width, int height) {
        currentFilter.onInputSizeChanged(width, height);
        leftFilter.onInputSizeChanged(width, height);
        rightFilter.onInputSizeChanged(width, height);

        currentFilter.onDisplaySizeChanged(width, height);
        leftFilter.onDisplaySizeChanged(width, height);
        rightFilter.onDisplaySizeChanged(width, height);
    }

    public int getOutputTexture() {
        return mFrameTextureBuffers[0];
    }

    public void onDrawFrame(int textureId) {
        OpenGLUtils.glBindFrameTexture(mFrameBuffers[0], mFrameTextureBuffers[0]);
        if (direction == 0 && offset == 0) {
            currentFilter.onDrawFrame(textureId);
        } else if (direction == 1) { //右滑，从左向右
            onDrawSlideLeft(textureId);
        } else if (direction == -1) { //左滑  从右向左
            onDrawSlideRight(textureId);
        }
        OpenGLUtils.glUnbindFrameBuffer();
    }

    private void onDrawSlideRight(int textureId) {
        if (isLocked && mScroller.computeScrollOffset()) {
            offset = mScroller.getCurrX();
            drawSlideRight(textureId);
        } else {
            drawSlideRight(textureId);
            if (isLocked) {
                if (isNeedSwitch) {
                    reCreateLeftFilter();
                    if (mListener != null) {
                        mListener.onFilterChange(filterTypes[currentIndex]);
                    }

                }

                offset = 0;
                direction = 0;
                isLocked = false;
            }
        }
    }

    private void onDrawSlideLeft(int textureId) {
        if (isLocked && mScroller.computeScrollOffset()) {
            offset = mScroller.getCurrX();
            drawSlideLeft(textureId);
        } else {
            drawSlideLeft(textureId);
            if (isNeedSwitch) {
                reCreateRightFilter();
                if (mListener != null) {
                    mListener.onFilterChange(filterTypes[currentIndex]);
                }
            }
            offset = 0;
            direction = 0;
            isLocked = false;
        }
    }

    private void drawSlideRight(int textureId) {
        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);//启用裁剪测试
        GLES20.glScissor(0, 0, mWidth - offset, mHeight);
        currentFilter.onDrawFrame(textureId);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(mWidth - offset, 0, offset, mHeight);
        rightFilter.onDrawFrame(textureId);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

    }

    private void drawSlideLeft(int textureId) {
        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(0, 0, offset, mHeight);
        leftFilter.onDrawFrame(textureId);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(offset, 0, mWidth - offset, mHeight);
        currentFilter.onDrawFrame(textureId);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }

    private void reCreateRightFilter() {
        decreaseCurIndex();
        rightFilter.destory();
        rightFilter = currentFilter;
        currentFilter = leftFilter;
        leftFilter = getFilter(getLeftIndex());
        leftFilter.init();
        leftFilter.onDisplaySizeChanged(mWidth, mHeight);
        leftFilter.onInputSizeChanged(mWidth, mHeight);
        isNeedSwitch = false;
    }

    private void reCreateLeftFilter() {
        increaseCurIndex();
        leftFilter.destory();
        leftFilter = currentFilter;
        currentFilter = rightFilter;
        rightFilter = getFilter(getRightIndex());
        rightFilter.init();
        rightFilter.onInputSizeChanged(mWidth, mHeight);
        rightFilter.onDisplaySizeChanged(mWidth, mHeight);
        isNeedSwitch = false;
    }

    private void increaseCurIndex() {
        currentIndex++;
        if (currentIndex >= filterTypes.length) {
            currentIndex = 0;
        }
    }

    private void decreaseCurIndex() {
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = filterTypes.length - 1;
        }
    }


    private int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * 可以循环切换，
     * 滑到最右边的时候，回到第0个
     * 滑到最左边，滑到最右边
     *
     * @return
     */
    private int getRightIndex() {
        int rightIndex = currentIndex + 1;
        if (rightIndex >= filterTypes.length) {
            rightIndex = 0;
        }
        return rightIndex;
    }

    private int getLeftIndex() {
        int leftIndex = currentIndex - 1;
        if (leftIndex < 0) {
            leftIndex = filterTypes.length - 1;
        }
        return leftIndex;
    }


    public void onTouchEvent(MotionEvent event) {
        if (isLocked) {
            return;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (downX == -1) {
                    return;
                }
                int curX = (int) event.getX();
                if (curX > downX) {
                    direction = 1;//右滑
                } else {
                    direction = -1;//左滑
                }

                offset = Math.abs(curX - downX);
                break;
            case MotionEvent.ACTION_UP:
                if (downX == -1) {
                    return;
                }
                if (offset == 0) {
                    return;
                }
                isLocked = true;
                downX = -1;
                if (offset > Constants.screenWidth / 3) {//滑动的距离超过屏幕的三分之一的话，就认为下一个滤镜
                    mScroller.startScroll(offset, 0, Constants.screenWidth - offset, 0, 100 * (1 - offset / Constants.screenWidth));
                    isNeedSwitch = true;
                } else {
                    mScroller.startScroll(offset, 0, -offset, 0, 100 * (offset / Constants.screenWidth));
                    isNeedSwitch = false;
                }

                break;
        }
    }
    public void setOnFilterChangeListener(OnFilterChangeListener listener) {
        this.mListener = listener;
    }

    public interface OnFilterChangeListener {
        void onFilterChange(FilterType type);
    }
}
