package com.me.alpha.Profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.RequestBuilder;
import com.me.alpha.Home.Home_Get_Set;
import com.me.alpha.R;

import java.util.ArrayList;

public class MyVideos_Adapter extends RecyclerView.Adapter<MyVideos_Adapter.CustomViewHolder > {

    public Context context;
    private MyVideos_Adapter.OnItemClickListener listener;
    private ArrayList<Home_Get_Set> dataList;


    public interface OnItemClickListener {
        void onItemClick(int postion, Home_Get_Set item, View view);
    }

    public MyVideos_Adapter(Context context, ArrayList<Home_Get_Set> dataList, MyVideos_Adapter.OnItemClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;

    }

    @Override
    public MyVideos_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_myvideo_layout,null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        MyVideos_Adapter.CustomViewHolder viewHolder = new MyVideos_Adapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }



    class CustomViewHolder extends RecyclerView.ViewHolder {


        ImageView thumb_image;

        TextView view_txt;

        public CustomViewHolder(View view) {
            super(view);

            thumb_image=view.findViewById(R.id.thumb_image);
            view_txt=view.findViewById(R.id.view_txt);

        }

        public void bind(final int position,final Home_Get_Set item, final MyVideos_Adapter.OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(position,item,v);
                }
            });

        }

    }




    @Override
    public void onBindViewHolder(final MyVideos_Adapter.CustomViewHolder holder, final int i) {
        final Home_Get_Set item= dataList.get(i);
        holder.setIsRecyclable(false);

        /*ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(item.gif))
                .setCacheChoice(ImageRequest.CacheChoice.SMALL).build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController( holder.thumb_image.getController())
                .setAutoPlayAnimations(true)
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable anim) {
                     *//*   if (anim != null) {
                            if (anim instanceof AnimatedDrawable2) {
                                AnimatedDrawable2 animatedDrawable = (AnimatedDrawable2) anim;
                                animatedDrawable.setAnimationBackend(new LoopCountModifyingBackend(animatedDrawable.getAnimationBackend(), 100));
                            }
                        }*//*

                    }
                })
                .build();
        holder.thumb_image.setController(controller);

*/


        try {
            Glide.with(context)
                    .asGif()
                    .load(item.gif)
                    .skipMemoryCache(true)
                    .thumbnail(new RequestBuilder[]{Glide
                            .with(context)
                            .load(item.thum)})
                    .apply(RequestOptions.diskCacheStrategyOf( DiskCacheStrategy.RESOURCE)
                            .placeholder(context.getResources().getDrawable(R.drawable.image_placeholder)).centerCrop())

                    .into(holder.thumb_image);

        }catch (Exception e){

        }



        holder.view_txt.setText(item.views);

        holder.bind(i,item,listener);

    }

}