package lyq.com.magicvideorecord.camera.gpufilter.factory;

import lyq.com.magicvideorecord.camera.gpufilter.base.GPUImageFilter;
import lyq.com.magicvideorecord.camera.gpufilter.fliter.AntiqueFilter;
import lyq.com.magicvideorecord.camera.gpufilter.fliter.BrannanFilter;
import lyq.com.magicvideorecord.camera.gpufilter.fliter.CoolFilter;
import lyq.com.magicvideorecord.camera.gpufilter.fliter.FreudFilter;
import lyq.com.magicvideorecord.camera.gpufilter.fliter.HefeFilter;
import lyq.com.magicvideorecord.camera.gpufilter.fliter.HudsonFilter;
import lyq.com.magicvideorecord.camera.gpufilter.fliter.InkwellFilter;
import lyq.com.magicvideorecord.camera.gpufilter.fliter.NashvilleFilter;
import lyq.com.magicvideorecord.camera.gpufilter.fliter.SketchFilter;
import lyq.com.magicvideorecord.camera.gpufilter.fliter.WarmFilter;

/**
 * @author sunshiny
 * @date 2018/12/30.
 * @desc
 */
public class FilterFactory {
    private static FilterType filterType = FilterType.NONE;

    public static GPUImageFilter initFilters(FilterType type){

        if (type == null) {
            return null;
        }

        switch (type){
            case ANTIQUE:
                return new AntiqueFilter();
            case BRANNAN:
                return new BrannanFilter();
            case FREUD:
                return new FreudFilter();
            case HEFE:
                return new HefeFilter();
            case HUDSON:
                return new HudsonFilter();
            case INKWELL:
                return new InkwellFilter();
            case NASHVILLE:
                return new NashvilleFilter();
            case COOL:
                return new CoolFilter();
            case WARM:
                return new WarmFilter();
            case SKETCH:
                return new SketchFilter();
            default:
                return null;
        }
    }

    public static FilterType getFilterType() {
        return filterType;
    }
}
