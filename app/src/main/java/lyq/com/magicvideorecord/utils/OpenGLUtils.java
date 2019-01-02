package lyq.com.magicvideorecord.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.khronos.opengles.GL10;

import lyq.com.magicvideorecord.config.MyApplication;

/**
 * @author sunshiny
 * @date 2018/12/18.
 */
public class OpenGLUtils {

    public static final int NO_TEXTURE = -1;
    public static final int NOT_INIT = -1;
    public static final int ON_DRAWN = 1;

    /**
     * 读取资源文件
     * @param rawId
     * @return
     */
    public static String readRawTextFile(int rawId) {
        InputStream is = MyApplication.getContext().getResources().openRawResource(rawId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    /**
     * 创建G小程序
     *
     * @param vSource
     * @param fSource
     * @return
     */
    public static int loadProgram(String vSource, String fSource) {
        int vertext = loadShader(GLES20.GL_VERTEX_SHADER, vSource);
        if (vertext == 0) return 0;
        int fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fSource);
        if (fragment == 0) return 0;
        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertext);
            GLES20.glAttachShader(program, fragment);
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
//                GLES20.glDeleteProgram(program);
//                program = 0;
                throw new IllegalStateException("load program:" + GLES20.glGetProgramInfoLog(program));
            }
        }
        return program;
    }

    /**
     * 加载着色器
     *
     * @param shaderType
     * @param source
     * @return
     */
    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (0 != shader) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);

            int[] status = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
            if (status[0] != GLES20.GL_TRUE) {
                //失败
//                GLES20.glDeleteShader(shader);
//                shader = 0;
                throw new IllegalStateException("load shader:" + GLES20.glGetShaderInfoLog(shader));
            }
        }

        return shader;
    }


    /**
     * 创建纹理
     */
    public static int createTextureID() {
        int[] texture = new int[1];
        GLES20.glGenTextures(texture.length, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        //设置缩小过滤为使用纹理中坐标最接近的一个像素颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return texture[0];
    }

    /**
     * 创建纹理并配置
     *
     * @param textures
     */
    public static void glGenTextures(int[] textures) {
        GLES20.glGenTextures(textures.length, textures, 0);

        for (int i = 0; i < textures.length; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
            useTexParameter();
            //解绑
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }
    }

    public static void genTexturesWithParameter(int size, int[] textures, int start,
                                                int gl_format, int width, int height) {
        GLES20.glGenTextures(size, textures, start);
        for (int i = 0; i < size; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, gl_format, width, height,
                    0, gl_format, GLES20.GL_UNSIGNED_BYTE, null);
            useTexParameter();
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private static void useTexParameter() {
        /**
         * 过滤参数
         *  当纹理被使用到一个比他大 或者比他小的形状上的时候 该如何处理
         */
        // 放大
        // GLES20.GL_LINEAR  : 使用纹理中坐标附近的若干个颜色，通过平均算法 进行放大
        // GLES20.GL_NEAREST : 使用纹理坐标最接近的一个颜色作为放大的要绘制的颜色
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);

        /*设置纹理环绕方向*/
        //纹理坐标 一般用st表示，其实就是x y
        //纹理坐标的范围是0-1。超出这一范围的坐标将被OpenGL根据GL_TEXTURE_WRAP参数的值进行处理
        //GL_TEXTURE_WRAP_S, GL_TEXTURE_WRAP_T 分别为x，y方向。
        //GL_REPEAT:平铺
        //GL_MIRRORED_REPEAT: 纹理坐标是奇数时使用镜像平铺
        //GL_CLAMP_TO_EDGE: 坐标超出部分被截取成0、1，边缘拉伸
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

    }


    /**
     * 绑定fbo对象和其对应的纹理
     *
     * @param framebuffer
     * @param framebufferTexure
     */
    public static void glBindFrameTexture(int framebuffer, int framebufferTexure) {
        //将fbo对象与其纹理绑定
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer);
        //颜色附着
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, framebufferTexure, 0);
    }

    /**
     * 解绑
     */
    public static void glUnbindFrameBuffer() {
        //解绑
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }


    public static int loadTexture(final Context context, final String name){
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0){

            // Read in the resource
            final Bitmap bitmap = getImageFromAssetsFile(context,name);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0){
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    private static Bitmap getImageFromAssetsFile(Context context, String fileName){
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try{
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return image;
    }

}
