package lyq.com.magicvideorecord.camera.widget;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import lyq.com.magicvideorecord.R;
import lyq.com.magicvideorecord.camera.gpufilter.factory.FilterItem;

/**
 * @author sunshiny
 * @date 2018/12/26.
 * @desc
 */
public class FilterView extends FrameLayout {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private List<FilterItem> mItemList = new ArrayList<>();
    private FilterCallback mCallback;
    private int mCheckIndex = 0;


    public FilterView(@NonNull Context context) {
        super(context);
    }

    public FilterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FilterView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setItemList(List<FilterItem> itemList) {
        if (itemList == null) {
            return;
        }
        mItemList.clear();
        this.mItemList.addAll(itemList);
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }

    }

    public void setFilterCallback(FilterCallback callback) {
        this.mCallback = callback;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mRecyclerView = findViewById(R.id.recyclerview);
        mAdapter = new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_view_item, parent, false);
                return new RecyclerView.ViewHolder(view) {
                    @Override
                    public String toString() {
                        return super.toString();
                    }
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                CircleImageView filterImg = holder.itemView.findViewById(R.id.filter_img);
                CircleImageView filterCheck = holder.itemView.findViewById(R.id.filter_check);
                TextView filterTxt = holder.itemView.findViewById(R.id.filter_text);

                final FilterItem filterItem = mItemList.get(position);
                filterImg.setImageResource(filterItem.imgRes);
                filterTxt.setText(filterItem.name);
                filterCheck.setVisibility(mCheckIndex == position ? View.VISIBLE : View.GONE);
                holder.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCheckIndex = position;
                        if (mCallback != null) {
                            mCallback.onFilterSelect(filterItem);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return mItemList.size();
            }
        };
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        //设置分割线
        mRecyclerView.addItemDecoration(new SpaceItemDecration(getResources().getDimensionPixelSize(R.dimen.item_space)));
    }

    public void show() {
        int height=getResources().getDimensionPixelSize(R.dimen.filter_height);
        setTranslationY(height);
        setAlpha(0);
        setVisibility(View.VISIBLE);
        animate().setDuration(400).alpha(1f).translationY(0).setListener(null).start();
    }

    public void hide(){
        animate().setDuration(400).alpha(0f).translationY(getHeight()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }



    public interface FilterCallback {
        void onFilterSelect(FilterItem item);
    }
}
