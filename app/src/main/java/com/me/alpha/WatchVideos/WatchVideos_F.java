package com.me.alpha.WatchVideos;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.daasuu.gpuv.egl.filter.GlWatermarkFilter;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequest;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.tabs.TabLayout;
import com.me.alpha.Comments.Comment_F;
import com.me.alpha.Home.Home_Get_Set;
import com.me.alpha.KeyBoard.KeyboardHeightObserver;
import com.me.alpha.KeyBoard.KeyboardHeightProvider;
import com.me.alpha.Main_Menu.MainMenuFragment;
import com.me.alpha.Profile.Profile_F;
import com.me.alpha.R;
import com.me.alpha.SimpleClasses.API_CallBack;
import com.me.alpha.SimpleClasses.Fragment_Callback;
import com.me.alpha.SimpleClasses.Fragment_Data_Send;
import com.me.alpha.SimpleClasses.Functions;
import com.me.alpha.SimpleClasses.Variables;
import com.me.alpha.Taged.Taged_Videos_F;
import com.me.alpha.VideoAction.VideoAction_F;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.io.File;
import java.util.ArrayList;

public class WatchVideos_F extends AppCompatActivity implements Player.EventListener,
        KeyboardHeightObserver, View.OnClickListener, Fragment_Data_Send {

    Context context;

    RecyclerView recyclerView;
    ArrayList<Home_Get_Set> data_list;
    int currentPage=-1;
    LinearLayoutManager layoutManager;

    Watch_Videos_Adapter adapter;

    ProgressBar p_bar;

    private KeyboardHeightProvider keyboardHeightProvider;

    RelativeLayout write_layout;


    EditText message_edit;
    ImageButton send_btn;
    ProgressBar send_progress;

    public WatchVideos_F() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_watchvideo);
        context=this;

        if(Variables.sharedPreferences==null){
            Variables.sharedPreferences=getSharedPreferences(Variables.pref_name,Context.MODE_PRIVATE);
        }

        p_bar=findViewById(R.id.p_bar);

        Intent bundle=getIntent();
        data_list = (ArrayList<Home_Get_Set>) bundle.getSerializableExtra("arraylist");
        int position=bundle.getIntExtra("position",0);



        recyclerView=findViewById(R.id.recylerview);
        layoutManager=new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        SnapHelper snapHelper =  new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);


        adapter=new Watch_Videos_Adapter(context, data_list, new Watch_Videos_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion,final Home_Get_Set item, View view) {

                switch(view.getId()){

                    case R.id.user_pic:
                        onPause();

                        OpenProfile(item,false);
                        break;

                    case R.id.like_layout:
                        if(!item.liked.equals("1"))
                            Like_Video(postion,item);
                        break;

                    case R.id.comment_layout:
                        OpenComment(item);
                        break;

                    case R.id.shared_layout:
                        final VideoAction_F fragment = new VideoAction_F(item.video_url, new Fragment_Callback() {
                            @Override
                            public void Responce(Bundle bundle) {

                                if(bundle.getString("action").equals("save")){
                                    Save_Video(item);
                                }
                            }
                        });
                        fragment.show(getSupportFragmentManager(), "");
                        //  Functions.Share_through_app(WatchVideos_F.this,item.video_url);
                        break;
                }

            }
        });

        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);


        // this is the scroll listener of recycler view which will tell the current item number
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //here we find the current item number
                final int scrollOffset = recyclerView.computeVerticalScrollOffset();
                final int height = recyclerView.getHeight();
                int page_no=scrollOffset / height;

                if(page_no!=currentPage ){
                    currentPage=page_no;

                    Privious_Player();
                    Set_Player(currentPage);
                }

            }
        });

        recyclerView.scrollToPosition(position);



        findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        write_layout=findViewById(R.id.write_layout);
        message_edit=findViewById(R.id.message_edit);
        send_btn=findViewById(R.id.send_btn);
        send_btn.setOnClickListener(this);

        send_progress=findViewById(R.id.send_progress);

        keyboardHeightProvider = new KeyboardHeightProvider(this);


        findViewById(R.id.WatchVideo_F).post(new Runnable() {
            public void run() {

                keyboardHeightProvider.start();

            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
        keyboardHeightProvider.setKeyboardHeightObserver(this);
    }



    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {

        Log.d("resp",""+height);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(write_layout.getWidth(), write_layout.getHeight());
        params.bottomMargin = height;
        write_layout.setLayoutParams(params);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send_btn:
                if(Variables.sharedPreferences.getBoolean(Variables.islogin,false)) {

                    String comment_txt = message_edit.getText().toString();
                    if (!TextUtils.isEmpty(comment_txt)) {
                        Send_Comments(data_list.get(currentPage).video_id, comment_txt);
                    }

                }
                else {
                    Toast.makeText(context, "Please Login into app", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }




    @Override
    public void onDataSent(String yourData) {
        int comment_count =Integer.parseInt(yourData);
        Home_Get_Set item=data_list.get(currentPage);
        item.video_comment_count=""+comment_count;
        data_list.add(currentPage,item);
        adapter.notifyDataSetChanged();
    }





    public void Set_Player(final int currentPage){

        final Home_Get_Set item= data_list.get(currentPage);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "TikTok"));

        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(item.video_url));

        Log.d("resp",item.video_url);


        player.prepare(videoSource);

        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.addListener(this);



        View layout=layoutManager.findViewByPosition(currentPage);
        PlayerView playerView=layout.findViewById(R.id.playerview);
        playerView.setPlayer(player);


        player.setPlayWhenReady(true);
        privious_player=player;



        final RelativeLayout mainlayout = layout.findViewById(R.id.mainlayout);
        playerView.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    super.onFling(e1, e2, velocityX, velocityY);
                    float deltaX = e1.getX() - e2.getX();
                    float deltaXAbs = Math.abs(deltaX);
                    // Only when swipe distance between minimal and maximal distance value then we treat it as effective swipe
                    if((deltaXAbs > 100) && (deltaXAbs < 1000)) {
                        if(deltaX > 0)
                        {
                            OpenProfile(item,true);
                        }
                    }


                    return true;
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    super.onSingleTapUp(e);
                    if(!player.getPlayWhenReady()){
                        privious_player.setPlayWhenReady(true);
                    }else{
                        privious_player.setPlayWhenReady(false);
                    }


                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    Show_video_option(item);

                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    if(!player.getPlayWhenReady()){
                        privious_player.setPlayWhenReady(true);
                    }

                    if(Variables.sharedPreferences.getBoolean(Variables.islogin,false)) {

                        Show_heart_on_DoubleTap(item, mainlayout, e);
                        Like_Video(currentPage, item);

                    }else {
                        Toast.makeText(context, "Please Login into ", Toast.LENGTH_SHORT).show();
                    }
                    return super.onDoubleTap(e);

                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        TextView desc_txt=layout.findViewById(R.id.desc_txt);
        HashTagHelper.Creator.create(context.getResources().getColor(R.color.maincolor), new HashTagHelper.OnHashTagClickListener() {
            @Override
            public void onHashTagClicked(String hashTag) {

                OpenHashtag(hashTag);

            }
        }).handle(desc_txt);





        LinearLayout soundimage = (LinearLayout)layout.findViewById(R.id.sound_image_layout);
        Animation aniRotate = AnimationUtils.loadAnimation(context,R.anim.d_clockwise_rotation);
        soundimage.startAnimation(aniRotate);

    }


    // when we swipe for another video this will relaese the privious player
    SimpleExoPlayer privious_player;
    public void Privious_Player(){
        if(privious_player!=null) {
            privious_player.removeListener(this);
            privious_player.release();
        }
    }





    public void Show_heart_on_DoubleTap(Home_Get_Set item,final RelativeLayout mainlayout,MotionEvent e){

        int x = (int) e.getX()-100;
        int y = (int) e.getY()-100;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        final ImageView iv = new ImageView(getApplicationContext());
        lp.setMargins(x, y, 0, 0);
        iv.setLayoutParams(lp);
        if(item.liked.equals("1"))
            iv.setImageDrawable(getResources().getDrawable(
                    R.drawable.ic_like));
        else
            iv.setImageDrawable(getResources().getDrawable(
                    R.drawable.ic_like_fill));

        mainlayout.addView(iv);
        Animation fadeoutani = AnimationUtils.loadAnimation(context,R.anim.fade_out);

        fadeoutani.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainlayout.removeView(iv);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        iv.startAnimation(fadeoutani);

    }




    // this function will call for like the video and Call an Api for like the video
    public void Like_Video(final int position, final Home_Get_Set home_get_set){

        String action=home_get_set.liked;

        if(action.equals("1")){
            action="0";
            home_get_set.like_count=""+(Integer.parseInt(home_get_set.like_count) -1);
        }else {
            action="1";
            home_get_set.like_count=""+(Integer.parseInt(home_get_set.like_count) +1);
        }


        data_list.remove(position);
        home_get_set.liked=action;
        data_list.add(position,home_get_set);
        adapter.notifyDataSetChanged();


        Functions.Call_Api_For_like_video(this, home_get_set.video_id,action ,new API_CallBack() {

            @Override
            public void ArrayData(ArrayList arrayList) {

            }

            @Override
            public void OnSuccess(String responce) {

            }

            @Override
            public void OnFail(String responce) {

            }
        });
    }



    // this will open the comment screen
    private void OpenComment(Home_Get_Set item) {
        int comment_count=Integer.parseInt(item.video_comment_count);
        Fragment_Data_Send fragment_data_send=this;

        Comment_F comment_f = new Comment_F(comment_count,fragment_data_send);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("video_id",item.video_id);
        comment_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.WatchVideo_F, comment_f).commit();

    }



    // this will open the profile of user which have uploaded the currenlty running video
    private void OpenProfile(Home_Get_Set item,boolean from_right_to_left) {

        if(Variables.sharedPreferences.getString(Variables.u_id,"0").equals(item.fb_id)){

            TabLayout.Tab profile= MainMenuFragment.tabLayout.getTabAt(4);
            profile.select();

        }else {
            Profile_F profile_f = new Profile_F(false);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if(from_right_to_left)
                transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
            else
                transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);

            Bundle args = new Bundle();
            args.putString("user_id", item.fb_id);
            args.putString("user_name",item.first_name+" "+item.last_name);
            args.putString("user_pic",item.profile_pic);
            profile_f.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.WatchVideo_F, profile_f).commit();
        }


    }



    public void Send_Comments(String video_id,String comment){

        send_progress.setVisibility(View.VISIBLE);
        send_btn.setVisibility(View.GONE);

        Functions.Call_Api_For_Send_Comment(this, video_id,comment ,new API_CallBack() {
            @Override
            public void ArrayData(ArrayList arrayList) {

                message_edit.setText(null);
                send_progress.setVisibility(View.GONE);
                send_btn.setVisibility(View.VISIBLE);

                int comment_count=Integer.parseInt(data_list.get(currentPage).video_comment_count);
                comment_count++;
                onDataSent(""+comment_count);
            }

            @Override
            public void OnSuccess(String responce) {

            }

            @Override
            public void OnFail(String responce) {

            }
        });

    }




    // this will open the profile of user which have uploaded the currenlty running video
    private void OpenHashtag(String tag) {

        Taged_Videos_F taged_videos_f = new Taged_Videos_F();
        FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("tag", tag);
        taged_videos_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.WatchVideo_F, taged_videos_f).commit();

    }



    private void Show_video_option(final Home_Get_Set home_get_set) {

        final CharSequence[] options = { "Save Video","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Save Video"))

                {
                    if(Functions.Checkstoragepermision(WatchVideos_F.this))
                        Save_Video(home_get_set);

                }


                else if (options[item].equals("Cancel")) {

                    dialog.dismiss();

                }

            }

        });

        builder.show();

    }

    public void Save_Video(final Home_Get_Set item){

        Functions.Show_determinent_loader(context,false,false);
        PRDownloader.initialize(getApplicationContext());
        DownloadRequest prDownloader= PRDownloader.download(item.video_url, Environment.getExternalStorageDirectory() +"/Tittic/", item.video_id+"no_watermark"+".mp4")
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {

                        int prog=(int)((progress.currentBytes*100)/progress.totalBytes);
                        Functions.Show_loading_progress(prog/2);

                    }
                });


        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                Applywatermark(item);
            }

            @Override
            public void onError(Error error) {
                Delete_file_no_watermark(item);
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                Functions.cancel_determinent_loader();
            }

        });




    }

    public void Applywatermark(final Home_Get_Set item){

        Bitmap myLogo = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_watermark_image)).getBitmap();
        Bitmap bitmap_resize=Bitmap.createScaledBitmap(myLogo, 50, 50, false);
        GlWatermarkFilter filter=new GlWatermarkFilter(bitmap_resize, GlWatermarkFilter.Position.LEFT_TOP);
        new GPUMp4Composer(Environment.getExternalStorageDirectory() +"/Tittic/"+item.video_id+"no_watermark"+".mp4",
                Environment.getExternalStorageDirectory() +"/Tittic/"+item.video_id+".mp4")
                .filter(filter)

                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {

                        Log.d("resp",""+(int) (progress*100));
                        Functions.Show_loading_progress((int)((progress*100)/2)+50);

                    }

                    @Override
                    public void onCompleted() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Functions.cancel_determinent_loader();
                                Delete_file_no_watermark(item);
                                Scan_file(item);

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

                                    Delete_file_no_watermark(item);
                                    Functions.cancel_determinent_loader();
                                    Toast.makeText(context, "Try Again", Toast.LENGTH_SHORT).show();

                                }catch (Exception e){

                                }
                            }
                        });

                    }
                })
                .start();
    }


    public void Delete_file_no_watermark(Home_Get_Set item){
        File file=new File(Environment.getExternalStorageDirectory() +"/Tittic/"+item.video_id+"no_watermark"+".mp4");
        if(file.exists()){
            file.delete();
        }
    }

    public void Scan_file(Home_Get_Set item){
        MediaScannerConnection.scanFile(WatchVideos_F.this,
                new String[] { Environment.getExternalStorageDirectory() +"/Tittic/"+item.video_id+".mp4" },
                null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {

                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }



    // this is lifecyle of the Activity which is importent for play,pause video or relaese the player
    @Override
    public void onPause() {
        super.onPause();
        if(privious_player!=null){
            privious_player.setPlayWhenReady(false);
        }
        keyboardHeightProvider.setKeyboardHeightObserver(null);
    }


    @Override
    public void onStop() {
        super.onStop();
        if(privious_player!=null){
            privious_player.setPlayWhenReady(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(privious_player!=null){
            privious_player.release();
        }

        keyboardHeightProvider.close();
    }



    // Bottom all the function and the Call back listener of the Expo player
    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if(playbackState==Player.STATE_BUFFERING){
            p_bar.setVisibility(View.VISIBLE);
        }
        else if(playbackState==Player.STATE_READY){
            p_bar.setVisibility(View.GONE);
        }

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }


    @Override
    public void onSeekProcessed() {


    }




}
