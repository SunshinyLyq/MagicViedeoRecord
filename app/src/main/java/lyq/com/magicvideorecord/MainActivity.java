package lyq.com.magicvideorecord;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import lyq.com.magicvideorecord.camera.widget.CameraView;
import lyq.com.magicvideorecord.camera.widget.CircleProgressView;

public class MainActivity extends AppCompatActivity {

    private CameraView mCameraView;
    private CircleProgressView mCapture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
    }

}
