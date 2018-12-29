package lyq.com.magicvideorecord.camera.fliter;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import lyq.com.magicvideorecord.utils.MatrixUtils;
import lyq.com.magicvideorecord.utils.OpenGLUtils;

/**
 * @author sunshiny
 * @date 2018/12/14.
 * @description
 */
public abstract class AbstractFilter {

    private static final String TAG = "AbstractFilter";

    /**
     * 单位矩阵
     */
    public static final float[] OM = MatrixUtils.getOriginalMatrix();

    /**
     * 顶点坐标buffer
     */
    protected FloatBuffer mGLVertextBuffer;
    /**
     * 纹理坐标buffer
     */
    protected FloatBuffer mGLTextureBuffer;

    //顶点着色器
    protected int mVertextShaderId;
    //片元着色器
    protected int mFragmentShaderId;

    /**
     * GPU程序
     */
    protected int mGLProgramId;

    /**
     * 顶点坐标
     */
    protected int mVPosition;
    /**
     * 纹理坐标
     */
    protected int mVCoord;

    /**
     * 变换矩阵
     */
    protected int mVMatrix;

    private float[] matrix = Arrays.copyOf(OM, 16);
    /**
     * 纹理
     * Sampler2D
     * SamplerExternalOES
     */
    protected int mVTexture;

    private int textureId = 0;
    private int textureType = 0; // 默认使用Texture2D
    /**
     * 前后摄像头
     */
    protected int mFlag = 0;

    /**
     * 顶点坐标
     */
    float[] VERTEX = {
            -1.0f,  1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f,  -1.0f,
    };
    /**
     * 纹理坐标
     */
    float[] TEXTURE = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,

    };

    public AbstractFilter(Context context, int vertexShaderId, int fragmentShaderId) {
        this.mVertextShaderId = vertexShaderId;
        this.mFragmentShaderId = fragmentShaderId;
        initBuffer();
        createProgram(context);
    }

    //改变大小
    public final void setSize(int width, int height) {
        onSizeChanged(width, height);
    }

    public int getTextureId() {
        return textureId;
    }

    public void setFlag(int flag) {
        this.mFlag = flag;
    }

    public int getFlag() {
        return mFlag;
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    public int getTextureType() {
        return textureType;
    }

    protected abstract void onSizeChanged(int width, int height);

    public void draw() {
        onClear();
        onUseProgram();
        setExpandData();
        onBindTexture();
        onDraw();
    }

    /**
     * 画画，实际上就是传值的过程
     */
    private void onDraw() {
        GLES20.glEnableVertexAttribArray(mVPosition);
        GLES20.glVertexAttribPointer(mVPosition, 2, GLES20.GL_FLOAT, false, 0, mGLVertextBuffer);
        GLES20.glEnableVertexAttribArray(mVCoord);
        GLES20.glVertexAttribPointer(mVCoord, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mVPosition);
        GLES20.glDisableVertexAttribArray(mVCoord);
    }

    /**
     * 绑定默认纹理
     */
    protected void onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(mVTexture, textureType);
    }

    /**
     * 设置其他扩展数据，变换矩阵
     * 比如说一开始采集摄像头的数据使用的采样器就是sampler_external_2D，
     * 额外扩展的采样器，需要传入对应的变换矩阵，才能正确的采样
     */
    protected void setExpandData() {
        GLES20.glUniformMatrix4fv(mVMatrix, 1, false, matrix, 0);
    }

    /**
     * 使用GL程序
     */
    protected void onUseProgram() {
        GLES20.glUseProgram(mGLProgramId);
    }

    /**
     * 清除画布
     */
    protected void onClear() {
        //清理成黑色
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    public void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }

    /**
     * 初始化buffer
     */
    protected void initBuffer() {
        mGLVertextBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLVertextBuffer.put(VERTEX);
        mGLVertextBuffer.position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer.put(TEXTURE);
        mGLTextureBuffer.position(0);
    }

    /**
     * 创建GL程序以及初始化变量
     */
    protected void createProgram(Context context) {
        String vertextShader = OpenGLUtils.readRawTextFile(context, mVertextShaderId);
        String fragmentShader = OpenGLUtils.readRawTextFile(context, mFragmentShaderId);
        mGLProgramId = OpenGLUtils.loadProgram(vertextShader, fragmentShader);
        //获取着色器中变量
        mVPosition = GLES20.glGetAttribLocation(mGLProgramId, "vPosition");
        mVCoord = GLES20.glGetAttribLocation(mGLProgramId, "vCoord");
        mVMatrix = GLES20.glGetUniformLocation(mGLProgramId, "vMatrix");
        mVTexture = GLES20.glGetUniformLocation(mGLProgramId, "vTexture");
    }

}
