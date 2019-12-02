package com.me.alpha.Profile.UserVideos;

import android.annotation.SuppressLint;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.me.alpha.Home.Home_Get_Set;
import com.me.alpha.SimpleClasses.Variables;
import com.me.alpha.WatchVideos.WatchVideos_F;
import com.me.alpha.Profile.MyVideos_Adapter;
import com.me.alpha.Profile.Profile_F;
import com.me.alpha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserVideo_F extends Fragment {

    RecyclerView recyclerView;
    ArrayList<Home_Get_Set> data_list;
    MyVideos_Adapter adapter;

    View view;
    Context context;

    String user_id;

    public static int myvideo_count=0;

    public UserVideo_F() {

    }


    @SuppressLint("ValidFragment")
    public UserVideo_F(String user_id) {

        this.user_id=user_id;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_user_video, container, false);

        context=getContext();


        recyclerView=view.findViewById(R.id.recylerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(context,3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            recyclerView.setNestedScrollingEnabled(false);
        }else {
            recyclerView.setNestedScrollingEnabled(true);
        }

        data_list=new ArrayList<>();
        adapter=new MyVideos_Adapter(context, data_list, new MyVideos_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion,Home_Get_Set item, View view) {

                OpenWatchVideo(postion);

            }
        });

        recyclerView.setAdapter(adapter);

        Call_Api_For_get_Allvideos();



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            Profile_F.scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (!Profile_F.scrollView.canScrollVertically(1)) {
                        recyclerView.setNestedScrollingEnabled(true);

                        Log.d("resp","scroll-end");
                    }else {
                        recyclerView.setNestedScrollingEnabled(false);
                        Log.d("resp","scroll-left");
                    }

                }
            });
        }


        return view;

    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(view!=null && isVisibleToUser){
            Call_Api_For_get_Allvideos();


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                Profile_F.scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                    @Override
                    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                        if (!Profile_F.scrollView.canScrollVertically(1)) {
                            recyclerView.setNestedScrollingEnabled(true);

                            Log.d("resp","scroll-end");
                        }else {
                            recyclerView.setNestedScrollingEnabled(false);
                            Log.d("resp","scroll-left");
                        }

                    }
                });
            }


        }
    }


    //this will get the all videos data of user and then parse the data
    private void Call_Api_For_get_Allvideos() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("my_fb_id", Variables.sharedPreferences.getString(Variables.u_id,""));
            parameters.put("fb_id", user_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("resp",parameters.toString());

        RequestQueue rq = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, Variables.showMyAllVideos, parameters, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String respo=response.toString();
                        Log.d("responce",respo);
                        Parse_data(respo);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(context, "Something wrong with Api", Toast.LENGTH_SHORT).show();
                        Log.d("respo",error.toString());
                    }
                });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(120000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rq.getCache().clear();
        rq.add(jsonObjectRequest);

    }

    public void Parse_data(String responce){

        data_list.clear();

        try {
            JSONObject jsonObject=new JSONObject(responce);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                JSONArray msgArray=jsonObject.getJSONArray("msg");
                JSONObject data=msgArray.getJSONObject(0);
                JSONObject user_info=data.optJSONObject("user_info");



                JSONArray user_videos=data.getJSONArray("user_videos");
                if(!user_videos.toString().equals("["+"0"+"]")){
                    for (int i=0;i<user_videos.length();i++) {
                        JSONObject itemdata = user_videos.optJSONObject(i);

                        Home_Get_Set item=new Home_Get_Set();
                        item.fb_id=data.optString("fb_id");

                        item.first_name=user_info.optString("first_name");
                        item.last_name=user_info.optString("last_name");
                        item.profile_pic=user_info.optString("profile_pic");

                        JSONObject count=itemdata.optJSONObject("count");
                        item.like_count=count.optString("like_count");
                        item.video_comment_count=count.optString("video_comment_count");
                        item.views=count.optString("view");

                        JSONObject sound_data=itemdata.optJSONObject("sound");
                        item.sound_id=sound_data.optString("id");
                        item.sound_name=sound_data.optString("sound_name");
                        item.sound_pic=sound_data.optString("thum");


                        item.video_id=itemdata.optString("id");
                        item.liked=itemdata.optString("liked");
                        item.gif=Variables.base_url+itemdata.optString("gif");
                        item.video_url=Variables.base_url+itemdata.optString("video");
                        item.thum=Variables.base_url+itemdata.optString("thum");
                        item.created_date=itemdata.optString("created");

                        item.video_description=itemdata.optString("description");


                        data_list.add(item);
                    }

                    myvideo_count=data_list.size();
                }




                adapter.notifyDataSetChanged();

            }else {
                Toast.makeText(context, ""+jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }

    }





    private void OpenWatchVideo(int postion) {

        Intent intent=new Intent(getActivity(), WatchVideos_F.class);
        intent.putExtra("arraylist", data_list);
        intent.putExtra("position",postion);
        startActivity(intent);

    }



}
