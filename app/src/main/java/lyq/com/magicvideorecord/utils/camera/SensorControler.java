package lyq.com.magicvideorecord.utils.camera;

import android.app.Activity;
import android.app.Application;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Calendar;

import lyq.com.magicvideorecord.config.MyApplication;

/**
 * @author sunshiny
 * @date 2018/12/22.
 * @desc 加速度控制器 用来控制对焦
 */
public class SensorControler implements SensorEventListener {

    private static final String TAG = "SensorControler";

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private int mX;
    private int mY;
    private int mZ;
    private long lastStaticStamp = 0;

    private Calendar mCalender;

    //内部是否能够对焦控制
    boolean isFocusing = false;
    boolean canFocusIn = false;
    boolean canFocus = false;

    private CameraFocusListener mCameraFocusListener;

    public static final int DELAY_DURATION = 500;

    public static final int STATUS_NONE = 0;
    public static final int STATUS_STATIC = 1;
    public static final int STATUS_MOVE = 2;
    private int STATUE = STATUS_NONE;

    private static SensorControler mInstance;
    private int foucsing = 1;  //1 表示没有被锁定 0表示被锁定

    public SensorControler() {
        mSensorManager = (SensorManager) MyApplication.getContext().getSystemService(Activity.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public static SensorControler getInstance() {
        if (mInstance == null) {
            mInstance = new SensorControler();
        }

        return mInstance;
    }

    public void setCameraFocusListener(CameraFocusListener mCameraFocusListener) {
        this.mCameraFocusListener = mCameraFocusListener;
    }

    public void onStart() {
        resetParams();
        canFocus = true;
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onStop() {
        mSensorManager.unregisterListener(this, mSensor);
        canFocus = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null) {
            return;
        }

        if (isFocusing) {
            resetParams();
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            int x = (int) event.values[0];
            int y = (int) event.values[1];
            int z = (int) event.values[2];
            mCalender = Calendar.getInstance();

            long stamp = mCalender.getTimeInMillis();

            if (STATUE != STATUS_NONE) {
                int px = Math.abs(mX - x);
                int py = Math.abs(mY - y);
                int pz = Math.abs(mZ - z);

                double value = Math.sqrt(px * px + py * py + pz * pz);
                if (value > 1.4) {
                    STATUE = STATUS_MOVE;
                } else {
                    //上一次的状态是move,记录静态时间点
                    if (STATUE == STATUS_MOVE) {
                        lastStaticStamp = stamp;
                        canFocusIn = true;
                    }

                    if (canFocusIn) {
                        if (stamp - lastStaticStamp > DELAY_DURATION) {
                            //移动后静止一段时间可以发生对焦行为
                            if (!isFocusing) {
                                canFocusIn = false;
                                if (mCameraFocusListener != null) {
                                    mCameraFocusListener.onFocus();
                                }
                            }
                        }
                        STATUE = STATUS_STATIC;
                    }

                }
            } else {
                lastStaticStamp = stamp;
                STATUE = STATUS_STATIC;
            }

            mX = x;
            mY = y;
            mZ = z;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void resetParams() {
        STATUE = STATUS_NONE;
        canFocusIn = false;
        mX = 0;
        mY = 0;
        mZ = 0;
    }

    /**
     * 对焦是否被锁定
     *
     * @return
     */
    public boolean isFocusLocked() {
        if (canFocus) {
            return foucsing <= 0;
        }

        return false;
    }

    /**
     * 锁定对焦
     */
    public void lockFocus() {
        isFocusing = true;
        foucsing--;
    }

    /**
     * 解锁对焦
     */
    public void unlockFocus() {
        isFocusing = false;
        foucsing++;
    }

    public interface CameraFocusListener {
        void onFocus();
    }

}
