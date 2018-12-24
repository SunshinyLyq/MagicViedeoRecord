package lyq.com.magicvideorecord.camera.widget;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import lyq.com.magicvideorecord.camera.render.CameraRender;
import lyq.com.magicvideorecord.utils.camera.CameraHelper;

/**
 * @date 2018/12/14.
 * @description 开启摄像头，
 * 预览数据,初始化opengl
 */
public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private CameraHelper mCameraHelper;
    private CameraRender mCameraRender;
    private Context mContext;
    private int dataWidth = 0;
    private int dataHeight = 0;

    private boolean isSetParam = false;


    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        /**初始化OpenGL的相关信息*/
        setEGLContextClientVersion(2);//配置EGL版本
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);//设置渲染模式,按需渲染
        setPreserveEGLContextOnPause(true);//当pause的时候保存Context
        setCameraDistance(100);//设置相机距离

        /**初始化相机管理类*/
        mCameraHelper = new CameraHelper(Camera.CameraInfo.CAMERA_FACING_BACK, getContext());

        /**初始化相机绘画类*/
        mCameraRender = new CameraRender(mContext);
    }

    /**
     * 打开摄像头
     */
    private void open() {
        mCameraHelper.stopPreview();
        mCameraHelper.open();

        Point previewSize = mCameraHelper.getPreSize();
        dataWidth = previewSize.x;
        dataHeight = previewSize.y;

        SurfaceTexture surfaceTexture=mCameraRender.getSurfaceTxure();
        surfaceTexture.setOnFrameAvailableListener(this);
        mCameraHelper.startPreview(surfaceTexture);//设置预览数据
    }


    /**
     * 切换摄像头
     */
    public void switchCamera() {
        mCameraHelper.switchCamera();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCameraRender.onSurfaceCreated(gl,config);
        if (!isSetParam) {
            open();
            stickerInit();
        }
        mCameraRender.setPreviewSize(dataWidth,dataHeight);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCameraRender.onSurfaceChanged(gl,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (isSetParam){
            mCameraRender.onDrawFrame(gl);
        }

    }

    /**
     * 每次Activity onResume时被调用，第一次不会打开相机
     */
    @Override
    public void onResume() {
        super.onResume();

        if (isSetParam){
            open();
        }
    }

    public void onDestroy() {
        if (mCameraHelper != null) {
            mCameraHelper.stopPreview();
        }
    }


    //设置已经设置好摄像头数据了
    private void stickerInit() {
        if (!isSetParam && dataHeight > 0 && dataWidth > 0) {
            isSetParam = true;
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.requestRender();
    }
}
