package lyq.com.magicvideorecord.camera.bean;

import lyq.com.magicvideorecord.camera.gpufilter.factory.FilterType;

/**
 * @author sunshiny
 * @date 2018/12/26.
 * @desc
 */
public class FilterItem {

    public int imgRes;
    public String name;
    public FilterType filterType;

    public FilterItem(int imgRes, String name, FilterType filterType) {
        this.imgRes = imgRes;
        this.name = name;
        this.filterType = filterType;
    }

    // TODO: 2018/12/26 根据不同的滤镜创建对应的类
    public void initFilter(){

    }
}
