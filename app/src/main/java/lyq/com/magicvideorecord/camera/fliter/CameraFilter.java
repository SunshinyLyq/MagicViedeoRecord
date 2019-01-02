package lyq.com.magicvideorecord.camera.fliter;

import android.content.Context;

/**
 * @author sunshiny
 * @date 2018/12/18.
 */
public class CameraFilter extends OesFilter {


    public CameraFilter(Context context) {
        super(context);
    }

    @Override
    public void setFlag(int flag) {
        super.setFlag(flag);
        float[] texture;

        if (getFlag() == 1) {//前置摄像头，顺时针旋转90度，再颠倒
            texture = new float[]{
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 0.0f,
            };
        } else { // 后置摄像头
            texture = new float[]{
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f,
            };
        }

        mGLTextureBuffer.clear();
        mGLTextureBuffer.put(texture);
        mGLTextureBuffer.position(0);
    }
}
