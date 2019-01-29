package lyq.com.magicvideorecord;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lyq.com.magicvideorecord.camera.gpufilter.base.SlideGroupFilter;
import lyq.com.magicvideorecord.camera.gpufilter.factory.FilterItem;
import lyq.com.magicvideorecord.camera.gpufilter.factory.FilterType;
import lyq.com.magicvideorecord.camera.widget.BeautyView;
import lyq.com.magicvideorecord.camera.widget.CameraView;
import lyq.com.magicvideorecord.camera.widget.CircleProgressView;
import lyq.com.magicvideorecord.camera.widget.FilterView;
import lyq.com.magicvideorecord.camera.widget.FocusImageView;
import lyq.com.magicvideorecord.config.Constants;
import lyq.com.magicvideorecord.utils.camera.SensorControler;

public class MainActivity extends AppCompatActivity implements SensorControler.CameraFocusListener, View.OnClickListener, View.OnTouchListener, RadioGroup.OnCheckedChangeListener, FilterView.FilterCallback, BeautyView.onBeautyChangeCallback, SlideGroupFilter.OnFilterChangeListener {

    private static final String TAG = "MainActivity";

    private CameraView mCameraView;
    private CircleProgressView mCapture;
    private FocusImageView mFocus;
    private ImageView mBeautyBtn;
    private ImageView mFilterBtn;
    private ImageButton mSwitchCamera;
    private ImageButton mSpeed;
    private RadioGroup rg_speed;
    private FilterView mFilterView;
    private BeautyView mBeautyView;
    private LinearLayout ll_record;
    private ImageView mStickerBtn;
    private ImageButton mMore;
    private LinearLayout ll_camera_fuction;


    private static final int MAX_RECORD_TIME = 15000;//最长录制15s
    private boolean recordFlag = false;//是都正在录制视频
    private boolean pausing = false;//是否暂停
    private boolean autoPausing = false;//是否是因为已经到了15s,才暂停的
    long timeCount = 0; //用来记录录制时间
    private long timpStep = 50;//进度条刷新的时间


    ExecutorService mExecutorService;//线程池
    private SensorControler mSensorControler;
    private List<FilterItem> mFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mExecutorService = Executors.newSingleThreadExecutor();
        mSensorControler = SensorControler.getInstance();
        mSensorControler.setCameraFocusListener(this);
        initView();
        initFilter();

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
        ll_record = findViewById(R.id.ll_record);
        mStickerBtn = findViewById(R.id.btn_sticker);
        mMore = findViewById(R.id.btn_more);
        ll_camera_fuction = findViewById(R.id.ll_camera_function);


        mCameraView.setOnTouchListener(this);
        mCameraView.setOnFilterChangeListener(this);
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
                beauty();
                break;
            //滤镜
            case R.id.btn_camera_filter:
                filter();
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

    private void beauty() {
        if (mBeautyView == null) {
            ViewStub viewStub = findViewById(R.id.beauty_stub);
            mBeautyView = (BeautyView) viewStub.inflate();
            mBeautyView.setVisibility(View.GONE);
            mBeautyView.setCallback(this);
        }
        ll_record.setVisibility(View.GONE);
        ll_camera_fuction.setVisibility(View.GONE);
        rg_speed.setVisibility(View.GONE);
        mBeautyView.show();
    }

    private void initFilter() {
        mFilters = new LinkedList<>();
        mFilters.add(new FilterItem(R.mipmap.filter_thumb_default, "None", FilterType.NONE));
        mFilters.add(new FilterItem(R.mipmap.filter_thumb_warm, "warm", FilterType.WARM));
        mFilters.add(new FilterItem(R.mipmap.filter_thumb_antique, "antique", FilterType.ANTIQUE));
        mFilters.add(new FilterItem(R.mipmap.filter_thumb_inkwell, "inkwell", FilterType.INKWELL));
        mFilters.add(new FilterItem(R.mipmap.filter_thumb_brannan, "brannan", FilterType.BRANNAN));
        mFilters.add(new FilterItem(R.mipmap.filter_thumb_freud, "freud", FilterType.FREUD));
        mFilters.add(new FilterItem(R.mipmap.filter_thumb_hefe, "hefe", FilterType.HEFE));
        mFilters.add(new FilterItem(R.mipmap.filter_thumb_hudson, "hudson", FilterType.HUDSON));
        mFilters.add(new FilterItem(R.mipmap.filter_thumb_nashville, "nashville", FilterType.NASHVILLE));
        mFilters.add(new FilterItem(R.mipmap.filter_thumb_cool, "cool", FilterType.COOL));
        mFilters.add(new FilterItem(R.mipmap.filter_thumb_sketch, "sketch", FilterType.SKETCH));
    }

    private void filter() {
        if (mFilterView == null) {
            ViewStub stub = findViewById(R.id.filter_stub);
            mFilterView = (FilterView) stub.inflate();
            mFilterView.setVisibility(View.GONE);
            mFilterView.setItemList(mFilters);
            mFilterView.setFilterCallback(this);
        }
        ll_record.setVisibility(View.GONE);
        ll_camera_fuction.setVisibility(View.GONE);
        rg_speed.setVisibility(View.GONE);
        mFilterView.show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mFilterView != null && mFilterView.getVisibility() == View.VISIBLE
                    && !checkInArea(mFilterView, ev)) {
                mFilterView.hide();
                ll_record.setVisibility(View.VISIBLE);
                ll_camera_fuction.setVisibility(View.VISIBLE);
                return true;
            }

            if (mBeautyView != null && mBeautyView.getVisibility() == View.VISIBLE
                    && !checkInArea(mBeautyView, ev)) {
                mBeautyView.hide();
                ll_record.setVisibility(View.VISIBLE);
                ll_camera_fuction.setVisibility(View.VISIBLE);
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean checkInArea(View view, MotionEvent event) {
        int[] loc = new int[2];
        view.getLocationInWindow(loc);
        return event.getRawY() > loc[1];

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
        mCameraView.onTouch(event);
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

    // TODO: 2018/12/26 滤镜选择
    @Override
    public void onFilterSelect(FilterItem item) {
        mCameraView.setFilterSelect(item);
    }


    // TODO: 2018/12/27 美白磨皮
    @Override
    public void onBeautyChange(int ration) {
        mCameraView.onBeautyChange(ration);
    }

    //瘦脸
    @Override
    public void onFaceThinChange(int ration) {
    }

    //大眼
    @Override
    public void onBigEyeChange(int ratio) {

    }

    @Override
    public void onFilterChange(final FilterType type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if (type == FilterType.NONE) {
////                    Toast.makeText(MainActivity.this, "当前没有设置滤镜--" + type, Toast.LENGTH_SHORT).show();
//                } else {
////                    Toast.makeText(MainActivity.this, "当前滤镜切换为--" + type, Toast.LENGTH_SHORT).show();
//                }
            }
        });
    }
}
