package lyq.com.magicvideorecord.utils.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import lyq.com.magicvideorecord.config.Constants;
import lyq.com.magicvideorecord.utils.DensityUtils;

/**
 * @author sunshiny
 * @date 2018/12/14.
 * @description 相机的管理类
 */
public class CameraHelper implements Camera.PreviewCallback {

    private static final String TAG = "CameraHelper";

    private Context context;
    //预览尺寸
    public Camera.Size preSize;
    private int mCameraId;
    private Camera mCamera;
    private byte[] buffer;
    private Camera.PreviewCallback mPreviewCallback;
    private SurfaceTexture mSurfaceTexture;

    private Point mPreSize;


    public CameraHelper(int cameraId, Context context) {
        mCameraId = cameraId;
        this.context = context;
    }

    public void switchCamera() {
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        stopPreview();
        open();
        startPreview(mSurfaceTexture);
    }

    public int getCameraId() {
        return mCameraId;
    }

    public void stopPreview() {
        if (mCamera != null) {
            //预览数据回调接口
            mCamera.setPreviewCallback(null);
            //停止预览
            mCamera.stopPreview();
            //释放摄像头
            mCamera.release();
            mCamera = null;
        }
    }

    public void open() {
        //获得camera对象
        mCamera = Camera.open(mCameraId);
        //配置camera的属性
        Camera.Parameters parameters = mCamera.getParameters();
        //设置预览数据格式为nv21
        parameters.setPreviewFormat(ImageFormat.NV21);
        preSize = getCloselyPreSize(true, DensityUtils.getScreenWidth(context), DensityUtils.getScreenHeight(context), parameters.getSupportedPreviewSizes());
        //这是摄像头宽、高
        parameters.setPreviewSize(preSize.width, preSize.height);
        // 设置摄像头 图像传感器的角度、方向
        mCamera.setParameters(parameters);

        Camera.Size pre = parameters.getPreviewSize();
        mPreSize = new Point(pre.height, pre.width);
    }

    public void startPreview(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
        try {

            buffer = new byte[preSize.width * preSize.height * 3 / 2];
            //数据缓存区
            mCamera.addCallbackBuffer(buffer);
            mCamera.setPreviewCallbackWithBuffer(this);
            //设置预览画面
            mCamera.setPreviewTexture(mSurfaceTexture);
            mCamera.startPreview();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        mPreviewCallback = previewCallback;
    }

    public Point getPreSize() {
        return mPreSize;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // data数据依然是倒的
        if (null != mPreviewCallback) {
            mPreviewCallback.onPreviewFrame(data, camera);
        }
        camera.addCallbackBuffer(buffer);
    }

    /**
     * 手动聚焦
     *
     * @param point    触屏坐标，必须传入转换后的坐标
     * @param callback
     */
    public void onFocus(Point point, Camera.AutoFocusCallback callback) {
        Camera.Parameters parameters = mCamera.getParameters();

        boolean supportFocus = true;
        boolean supportMetering = true;

        //不支持设置自定义聚焦，则使用自动聚焦
        if (parameters.getMaxNumFocusAreas() <= 0) {
            supportFocus = false;
        }
        if (parameters.getMaxNumMeteringAreas() <= 0) {
            supportMetering = false;
        }

        //对焦和测光区域
        List<Camera.Area> areas = new ArrayList<>();
        List<Camera.Area> areas1 = new ArrayList<>();

        //把屏幕坐标，转换成Camera.Area中的坐标
        point.x = (int) (((float) point.x) / Constants.screenWidth * 2000 - 1000);
        point.y = (int) (((float) point.y) / Constants.screenHeight * 2000 - 1000);


        int left = point.x - 300;
        int top = point.y - 300;
        int right = point.x + 300;
        int bottom = point.y + 300;


        left = left < -1000 ? -1000 : left;
        top = top < -1000 ? -1000 : top;
        right = right > 1000 ? 1000 : right;
        bottom = bottom > 1000 ? 1000 : bottom;

        areas.add(new Camera.Area(new Rect(left,top,right,bottom),100));
        areas1.add(new Camera.Area(new Rect(left,top,right,bottom),100));

        if (supportFocus){
            parameters.setFocusAreas(areas);
        }
        if (supportMetering){
            parameters.setMeteringAreas(areas1);
        }

        try {
            mCamera.setParameters(parameters);
            mCamera.autoFocus(callback);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    /**
     * 获取最接近的尺寸
     *
     * @param isPortrait
     * @param surfaceWidth
     * @param surfaceHeight
     * @param preSizeList
     * @return
     */
    public static Camera.Size getCloselyPreSize(boolean isPortrait, int surfaceWidth, int surfaceHeight, List<Camera.Size> preSizeList) {

        int reqTmpWidth;
        int reqTmpHeight;

        //当屏幕为位置的时候，将宽高进行调换，保证宽>高
        if (isPortrait) {
            reqTmpWidth = surfaceHeight;
            reqTmpHeight = surfaceWidth;
        } else {
            reqTmpWidth = surfaceWidth;
            reqTmpHeight = surfaceHeight;
        }

        //先去查找preview中是否存在与Surface宽高相同的尺寸
        for (Camera.Size size : preSizeList) {
            if (size.width == reqTmpWidth && size.height == reqTmpHeight) {
                return size;
            }
        }

        //没有的话，就去找宽高比例最接近的那个尺寸
//        float reqRatio =(float)reqTmpWidth/(float)reqTmpHeight;
        float reqRatio = 1.778f;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;

        Camera.Size retSize = null;

        for (Camera.Size size : preSizeList) {
            curRatio = (float) size.width / (float) size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }

        return retSize;
    }
}
