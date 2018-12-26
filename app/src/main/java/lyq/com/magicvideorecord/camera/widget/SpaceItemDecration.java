package lyq.com.magicvideorecord.camera.widget;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author sunshiny
 * @date 2018/12/26.
 * @desc
 */
public class SpaceItemDecration extends RecyclerView.ItemDecoration {

    private int mSpace;

    public SpaceItemDecration(int mSpace) {
        this.mSpace = mSpace;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int pos= ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
        if (pos == 0){
            outRect.left = mSpace;
        }

        outRect.right = mSpace;
    }
}
