package lyq.com.magicvideorecord.camera.gpufilter.factory;

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
}
