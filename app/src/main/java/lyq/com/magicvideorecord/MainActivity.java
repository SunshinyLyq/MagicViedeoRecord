package lyq.com.magicvideorecord;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lyq.com.magicvideorecord.camera.widget.CameraView;
import lyq.com.magicvideorecord.camera.widget.CircleProgressView;
import lyq.com.magicvideorecord.camera.widget.FocusImageView;
import lyq.com.magicvideorecord.config.Constants;
import lyq.com.magicvideorecord.utils.camera.SensorControler;

public class MainActivity extends AppCompatActivity implements SensorControler.CameraFocusListener, View.OnClickListener, View.OnTouchListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "MainActivity";

    private CameraView mCameraView;
    private CircleProgressView mCapture;
    private FocusImageView mFocus;
    private ImageView mBeautyBtn;
    private ImageView mFilterBtn;
    private ImageButton mSwitchCamera;
    private ImageButton mSpeed;
    private RadioGroup rg_speed;

    private static final int MAX_RECORD_TIME = 15000;//最长录制15s
    private boolean recordFlag = false;//是都正在录制视频
    private boolean pausing = false;//是否暂停
    private boolean autoPausing = false;//是否是因为已经到了15s,才暂停的
    long timeCount = 0; //用来记录录制时间
    private long timpStep = 50;//进度条刷新的时间


    ExecutorService mExecutorService;//线程池
    private SensorControler mSensorControler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mExecutorService = Executors.newSingleThreadExecutor();
        mSensorControler = SensorControler.getInstance();
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
        mSpeed = findViewById(R.id.btn_speed);
        rg_speed = findViewById(R.id.rg_speed);


        mCameraView.setOnTouchListener(this);
        mBeautyBtn.setOnClickListener(this);
        mFilterBtn.setOnClickListener(this);
        mSwitchCamera.setOnClickListener(this);
        mCapture.setTotal(MAX_RECORD_TIME);
        mCapture.setOnClickListener(this);
        mSpeed.setOnClickListener(this);
        rg_speed.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //切换摄像头
            case R.id.btn_camera_switch:
                mCameraView.switchCamera();
                break;
            //开启美颜
            case R.id.btn_camera_beauty:
                break;
            //滤镜
            case R.id.btn_camera_filter:
                break;
            //快慢速
            case R.id.btn_speed:
                if (rg_speed.getVisibility() == View.VISIBLE) {
                    rg_speed.setVisibility(View.GONE);
                } else {
                    rg_speed.setVisibility(View.VISIBLE);
                }
                break;
            //点击录制视频
            case R.id.capture:
                if (!recordFlag) {//如果没有在录制
                    //开始录制
                    mExecutorService.execute(recordRunnable);
                } else if (!pausing) {//如果是正在录制中，并且没有暂停，则暂停录制
                    mCameraView.pause(false);
                    pausing = true;
                } else {//重新开始录制，接着录
                    mCameraView.resume(false);
                    pausing = false;
                }
                break;

        }

    }


    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            recordFlag = false;
            pausing = false;
            autoPausing = false;
            timeCount = 0;
            try {
                mCameraView.startRecord();
                while (timeCount <= MAX_RECORD_TIME && recordFlag) {
                    if (pausing || autoPausing) {
                        continue;
                    }
                    mCapture.setProcess((int) timeCount);
                    Thread.sleep(timpStep);
                    timeCount += timpStep;
                }

                recordFlag = false;
                mCameraView.stopRecord();

                if (timeCount < 1500) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "录制时间太短了", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    recordComplete();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    //录制完成
    // TODO: 2018/12/26 录制完成
    private void recordComplete() {
        mCapture.setProcess(0);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                float sRawX = event.getRawX(); //表示在屏幕上的原始点
                float sRawY = event.getRawY();

                float rawY = sRawY * Constants.screenWidth / Constants.screenHeight;
                float temp = sRawX;
                float rawX = rawY;
                rawY = (Constants.screenWidth - temp) * Constants.screenHeight / Constants.screenWidth;

                Point point = new Point((int) rawX, (int) rawY);
                mCameraView.onFocus(point, callback);
                mFocus.startFocus(new Point((int) sRawX, (int) sRawY));
                break;
        }
        return true;
    }

    Camera.AutoFocusCallback callback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                mFocus.onFocusSuccess();
            } else {
                mFocus.onFocusFailed();
            }
        }
    };

    @Override
    public void onFocus() {
        Point point = new Point(Constants.screenWidth / 2, Constants.screenHeight / 2);
        mCameraView.onFocus(point, callback);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.onResume();
        if (recordFlag && autoPausing) {//已经录制完15秒了
            mCameraView.resume(true); //如果已经录制完成了，
            autoPausing = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (recordFlag && !pausing) {
            mCameraView.pause(true);
            autoPausing = true;
        }

        mCameraView.onPause();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_extra_slow://极慢
                mCameraView.setSpeed(CameraView.Speed.MODE_EXTRA_SLOW);
            case R.id.rb_slow://慢
                mCameraView.setSpeed(CameraView.Speed.MODE_SLOW);
            case R.id.rb_normal://正常
                mCameraView.setSpeed(CameraView.Speed.MODE_NORMAL);
            case R.id.rb_fast://快
                mCameraView.setSpeed(CameraView.Speed.MODE_FAST);
            case R.id.rb_extra_fast://极快
                mCameraView.setSpeed(CameraView.Speed.MODE_EXTRA_FAST);
        }

    }
}
