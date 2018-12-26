package lyq.com.magicvideorecord;

import android.graphics.Point;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import lyq.com.magicvideorecord.camera.widget.CameraView;
import lyq.com.magicvideorecord.camera.widget.CircleProgressView;
import lyq.com.magicvideorecord.camera.widget.FocusImageView;
import lyq.com.magicvideorecord.config.Constants;
import lyq.com.magicvideorecord.utils.camera.SensorControler;

public class MainActivity extends AppCompatActivity implements SensorControler.CameraFocusListener, View.OnClickListener, View.OnTouchListener {

    private static final String TAG = "MainActivity";

    private CameraView mCameraView;
    private CircleProgressView mCapture;
    private FocusImageView mFocus;
    private ImageView mBeautyBtn;
    private ImageView mFilterBtn;
    private ImageButton mSwitchCamera;
    private static final int MAX_RECORD_TIME = 15000;//最长录制15s


    private SensorControler mSensorControler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorControler =SensorControler.getInstance();
        mSensorControler.setCameraFocusListener(this);
        initView();
    }

    private void initView() {
        mCameraView = findViewById(R.id.camera_view);
        mCapture = findViewById(R.id.capture);
        mFocus = findViewById(R.id.focusImageView);
        mBeautyBtn = findViewById(R.id.btn_camera_beauty);
        mFilterBtn = findViewById(R.id.btn_camera_filter);
        mSwitchCamera = findViewById(R.id.btn_camera_switch);


        mCameraView.setOnTouchListener(this);
        mBeautyBtn.setOnClickListener(this);
        mFilterBtn.setOnClickListener(this);
        mSwitchCamera.setOnClickListener(this);
        mCapture.setTotal(MAX_RECORD_TIME);
        mCapture.setOnClickListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                float sRawX = event.getRawX(); //表示在屏幕上的原始点
                float sRawY = event.getRawY();

                float rawY = sRawY * Constants.screenWidth / Constants.screenHeight;
                float temp = sRawX;
                float rawX = rawY;
                rawY = (Constants.screenWidth - temp) * Constants.screenHeight / Constants.screenWidth;

                Point point = new Point((int) rawX,(int) rawY);
                mCameraView.onFocus(point,callback);
                mFocus.startFocus(new Point((int) sRawX, (int) sRawY));
                break;
        }
        return true;
    }

    Camera.AutoFocusCallback callback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success){
                mFocus.onFocusSuccess();
            }else{
                mFocus.onFocusFailed();
            }
        }
    };

    @Override
    public void onFocus() {
        Point point = new Point(Constants.screenWidth / 2, Constants.screenHeight / 2);
        mCameraView.onFocus(point,callback);
    }

    @Override
    public void onClick(View v) {

    }


}
