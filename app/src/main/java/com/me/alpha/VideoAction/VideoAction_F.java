package com.me.alpha.VideoAction;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.me.alpha.R;
import com.me.alpha.SimpleClasses.Fragment_Callback;

import java.util.Collections;
import java.util.List;

public class VideoAction_F extends BottomSheetDialogFragment implements View.OnClickListener {

    View view;
    Context context;
    RecyclerView recyclerView;

    Fragment_Callback fragment_callback;

    String video_url;

    ProgressBar progressBar;

    public VideoAction_F() {
    }

    @SuppressLint("ValidFragment")
    public VideoAction_F(String url, Fragment_Callback fragment_callback) {
        video_url=url;
        this.fragment_callback=fragment_callback;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_video_action, container, false);
        context=getContext();

        progressBar=view.findViewById(R.id.progress_bar);
        view.findViewById(R.id.save_video_layout).setOnClickListener(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Get_Shared_app();
            }
        },1000);

        return view;
    }

    VideoSharingApps_Adapter adapter;
    public void Get_Shared_app(){
        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(context, 5);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {


                    PackageManager pm=getActivity().getPackageManager();
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "https://google.com");

                    List<ResolveInfo> launchables=pm.queryIntentActivities(intent, 0);

                    Collections.sort(launchables,
                            new ResolveInfo.DisplayNameComparator(pm));

                    adapter=new VideoSharingApps_Adapter(context, launchables, new VideoSharingApps_Adapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int positon, ResolveInfo item, View view) {
                            Open_App(item);
                        }
                    });

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);
                        }
                    });


                }
                catch (Exception e){

                }
            }
        }).start();





    }


    public void Open_App(ResolveInfo resolveInfo) {

        try {

            ActivityInfo activity = resolveInfo.activityInfo;
            ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                    activity.name);
            Intent i = new Intent(Intent.ACTION_MAIN);

            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            i.setComponent(name);

            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, video_url);
            intent.setComponent(name);
            startActivity(intent);
        }catch (Exception e){

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.save_video_layout:
                Bundle bundle=new Bundle();
                bundle.putString("action","save");
                dismiss();
                fragment_callback.Responce(bundle);
                break;
        }
    }
}
