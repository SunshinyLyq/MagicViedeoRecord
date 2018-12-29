package lyq.com.magicvideorecord.utils;

import android.opengl.Matrix;

/**
 * @author sunshiny
 * @date 2018/12/29.
 * @desc
 */
public class MatrixUtils {

    /**
     * 获取需要显示的矩阵
     * @param matrix
     * @param imgWidth
     * @param imgHeight
     * @param viewWidth
     * @param viewHeight
     */
    public static void getShowMatrix(float[] matrix,int imgWidth,int imgHeight,int viewWidth,int
            viewHeight){
        if(imgHeight>0&&imgWidth>0&&viewWidth>0&&viewHeight>0){
            float sWhView=(float)viewWidth/viewHeight;
            float sWhImg=(float)imgWidth/imgHeight;
            float[] projection=new float[16];
            float[] camera=new float[16];
            if(sWhImg>sWhView){
                //正交投影
                //1.存储生成的矩阵元素，2.填充起始的偏移量，3.4.near面的left、right 对应的x坐标;5.6.near面的bottom、top 对应的y坐标;
                // 7.near面、far面与视点的距离;
                Matrix.orthoM(projection,0,-sWhView/sWhImg,sWhView/sWhImg,-1,1,1,3);
            }else{
                Matrix.orthoM(projection,0,-1,1,-sWhImg/sWhView,sWhImg/sWhView,1,3);
            }
            // View矩阵的空间位置
            Matrix.setLookAtM(camera,0,0,0,1,0,0,0,0,1,0);
            Matrix.multiplyMM(matrix,0,projection,0,camera,0);
        }
    }

    /**
     * 上下翻转
     * @param m
     * @param x
     * @param y
     * @return
     */
    public static float[] flip(float[] m,boolean x,boolean y){
        if(x||y){
            Matrix.scaleM(m,0,x?-1:1,y?-1:1,1);
        }
        return m;
    }

    public static float[] getOriginalMatrix(){
        return new float[]{
                1,0,0,0,
                0,1,0,0,
                0,0,1,0,
                0,0,0,1
        };
    }
}
