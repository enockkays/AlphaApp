package com.me.alpha.Video_Recording.GalleryVideos;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.me.alpha.SimpleClasses.Functions;
import com.me.alpha.SimpleClasses.Variables;
import com.me.alpha.Video_Recording.GallerySelectedVideo.GallerySelectedVideo_A;
import com.me.alpha.R;

import java.io.File;
import java.util.ArrayList;

public class GalleryVideos_A extends AppCompatActivity {

    ArrayList<GalleryVideo_Get_Set> data_list;
    public RecyclerView recyclerView;
    GalleryVideos_Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_videos);


        data_list = new ArrayList();


        recyclerView=findViewById(R.id.recylerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);


        data_list=new ArrayList<>();
        adapter=new GalleryVideos_Adapter(this, data_list, new GalleryVideos_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, GalleryVideo_Get_Set item, View view) {
                MediaMetadataRetriever retriever = new  MediaMetadataRetriever();
                Bitmap bmp = null;
                try
                {
                    retriever.setDataSource(item.video_path);
                    bmp = retriever.getFrameAtTime();
                    int videoHeight=bmp.getHeight();
                    int videoWidth=bmp.getWidth();

                    Log.d("resp",""+videoWidth+"---"+videoHeight);

                }catch (Exception e){

                }

                Chnage_Video_size(item);

            }
        });

        recyclerView.setAdapter(adapter);

        getAllVideoPath(this);


        findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                overridePendingTransition(R.anim.in_from_top,R.anim.out_from_bottom);

            }
        });

    }




    public  void  getAllVideoPath(Context context) {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Video.VideoColumns.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        //int vidsCount = 0;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                GalleryVideo_Get_Set item=new GalleryVideo_Get_Set();
                item.video_path=cursor.getString(0);
                long file_duration=getfileduration(Uri.parse(cursor.getString(0)));

                Log.d("resp",""+file_duration);

                if(file_duration>5000 && file_duration<19500){
                    item.video_time=change_sec_to_time(file_duration);
                    data_list.add(item);
                }

            }
            adapter.notifyDataSetChanged();
            cursor.close();
        }
    }



    // get the audio file duration that is store in our directory
    public long getfileduration(Uri uri) {
        try {

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this, uri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            final int file_duration = Integer.parseInt(durationStr);

            return file_duration;
        }
        catch (Exception e){

        }
        return 0;
    }


    public String change_sec_to_time(long file_duration){
        long second = (file_duration / 1000) % 60;
        long minute = (file_duration / (1000 * 60)) % 60;

        return String.format("%02d:%02d", minute, second);

    }



    public void Chnage_Video_size(GalleryVideo_Get_Set item){

        Functions.Show_determinent_loader(this,false,false);

        new GPUMp4Composer(item.video_path, Variables.gallery_resize_video)
                .size(540, 960)
                .videoBitrate((int) (0.25 * 16 * 540 * 960))
                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {

                        Log.d("resp",""+(int) (progress*100));
                        Functions.Show_loading_progress((int)(progress*100));

                    }

                    @Override
                    public void onCompleted() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Functions.cancel_determinent_loader();

                                Intent intent=new Intent(GalleryVideos_A.this, GallerySelectedVideo_A.class);
                                intent.putExtra("video_path",Variables.gallery_resize_video);
                                startActivity(intent);

                            }
                        });


                    }

                    @Override
                    public void onCanceled() {
                        Log.d("resp", "onCanceled");
                    }

                    @Override
                    public void onFailed(Exception exception) {

                        Log.d("resp",exception.toString());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    Functions.cancel_determinent_loader();

                                    Toast.makeText(GalleryVideos_A.this, "Try Again", Toast.LENGTH_SHORT).show();
                                }catch (Exception e){

                                }
                            }
                        });

                    }
                })
                .start();

    }



    @Override
    protected void onStart() {
        super.onStart();
        DeleteFile();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        DeleteFile();
    }

    public void DeleteFile(){
        File output = new File(Variables.outputfile);
        File output2 = new File(Variables.outputfile2);
        File output_filter_file = new File(Variables.output_filter_file);
        File gallery_resize_video = new File(Variables.gallery_resize_video);

        if(output.exists()){
            output.delete();
        }
        if(output2.exists()){

            output2.delete();
        }
        if(output_filter_file.exists()){
            output_filter_file.delete();
        }

        if(gallery_resize_video.exists()){
            gallery_resize_video.delete();
        }


    }

}
