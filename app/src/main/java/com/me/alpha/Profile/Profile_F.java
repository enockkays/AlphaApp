package com.me.alpha.Profile;

// This is the profile screen which is show in 5 tab as well as it is also call
// when we see the profile of other users

import android.annotation.SuppressLint;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;
import com.me.alpha.Chat.Chat_Activity;
import com.me.alpha.Following.Following_F;
import com.me.alpha.Main_Menu.MainMenuActivity;
import com.me.alpha.Main_Menu.RelateOnFragment_OnBack.RootFragment;
import com.me.alpha.Profile.Liked_Videos.Liked_Video_F;
import com.me.alpha.Profile.UserVideos.UserVideo_F;
import com.me.alpha.See_Full_Image_F;
import com.me.alpha.SimpleClasses.API_CallBack;
import com.me.alpha.SimpleClasses.Functions;
import com.me.alpha.SimpleClasses.Variables;
import com.me.alpha.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Profile_F extends RootFragment implements View.OnClickListener {

    View view;
    Context context;


    public TextView edit_profile_btn;
    public  TextView username,video_count_txt;
    public ImageView imageView;
    public  TextView follow_count_txt,fans_count_txt,heart_count_txt;

    ImageView back_btn,setting_btn;

    String user_id,user_name,user_pic;

    Bundle bundle;

    protected TabLayout tabLayout;

    protected ViewPager pager;

    private ViewPagerAdapter adapter;

    public boolean isdataload=false;


    RelativeLayout tabs_main_layout;

    LinearLayout top_layout;

    public static NestedScrollView scrollView;

    public  static String pic_url;

    public  boolean is_show_edit_profile=true;

    public  LinearLayout create_popup_layout;

    public Profile_F() {

    }


    @SuppressLint("ValidFragment")
    public Profile_F(boolean is_show_edit_profile) {
        this.is_show_edit_profile=is_show_edit_profile;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_profile, container, false);
        context=getContext();



        bundle=getArguments();
        if(bundle!=null){
            user_id=bundle.getString("user_id");
            user_name=bundle.getString("user_name");
            user_pic=bundle.getString("user_pic");
        }



        return init();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.user_image:
                OpenfullsizeImage(pic_url);
                break;

            case R.id.edit_profile_btn:

                if(Variables.sharedPreferences.getString(Variables.u_id,"")
                        .equals(user_id))
                    Open_Edit_profile();
                else {
                    Follow_unFollow_User();
                }

                break;

            case R.id.setting_btn:
                Open_Setting();
                break;

            case R.id.following_layout:
                Open_Following();
                break;

            case R.id.fans_layout:
                Open_Followers();
                break;

            case R.id.back_btn:
                getActivity().onBackPressed();
                break;
        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if((view!=null && isVisibleToUser) && !isdataload){
            if(Variables.sharedPreferences.getBoolean(Variables.islogin,false))
                init();
        }
        if((view!=null && isVisibleToUser) && isdataload){

            Call_Api_For_get_Allvideos();

        }

    }


    public View init(){

        username=view.findViewById(R.id.username);
        imageView=view.findViewById(R.id.user_image);
        imageView.setOnClickListener(this);

        video_count_txt=view.findViewById(R.id.video_count_txt);

        follow_count_txt=view.findViewById(R.id.follow_count_txt);
        fans_count_txt=view.findViewById(R.id.fan_count_txt);
        heart_count_txt=view.findViewById(R.id.heart_count_txt);



        setting_btn=view.findViewById(R.id.setting_btn);
        setting_btn.setOnClickListener(this);

        back_btn=view.findViewById(R.id.back_btn);
        back_btn.setOnClickListener(this);

        edit_profile_btn=view.findViewById(R.id.edit_profile_btn);
        edit_profile_btn.setOnClickListener(this);


        if(is_show_edit_profile) {
            username.setText(Variables.sharedPreferences.getString(Variables.f_name, "") + " " + Variables.sharedPreferences.getString(Variables.l_name, ""));
            pic_url = Variables.sharedPreferences.getString(Variables.u_pic, "null");

            try {
                Picasso.with(context).load(pic_url)
                        .resize(150, 150)
                        .placeholder(R.drawable.profile_image_placeholder)
                        .into(imageView);

            } catch (Exception e) {

            }

        }

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        pager = view.findViewById(R.id.pager);
        pager.setOffscreenPageLimit(2);

        adapter = new ViewPagerAdapter(getResources(), getChildFragmentManager());
        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);

        setupTabIcons();


        tabs_main_layout=view.findViewById(R.id.tabs_main_layout);
        top_layout=view.findViewById(R.id.top_layout);



        ViewTreeObserver observer = top_layout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                final int height=top_layout.getMeasuredHeight();

                top_layout.getViewTreeObserver().removeGlobalOnLayoutListener(
                        this);

                ViewTreeObserver observer = tabs_main_layout.getViewTreeObserver();
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams) tabs_main_layout.getLayoutParams();
                        params.height= (int) (tabs_main_layout.getMeasuredHeight()+ height);
                        tabs_main_layout.setLayoutParams(params);
                        tabs_main_layout.getViewTreeObserver().removeGlobalOnLayoutListener(
                                this);

                    }
                });

            }
        });


        scrollView=view.findViewById(R.id.scrollview);


        if(!is_show_edit_profile){
            back_btn.setVisibility(View.VISIBLE);
            setting_btn.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_send_black));
        }else {
            back_btn.setVisibility(View.GONE);
            setting_btn.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_black_dots));
        }


        create_popup_layout=view.findViewById(R.id.create_popup_layout);


        view.findViewById(R.id.following_layout).setOnClickListener(this);
        view.findViewById(R.id.fans_layout).setOnClickListener(this);

        isdataload=true;


        Call_Api_For_get_Allvideos();

        return view;
    }


    private void setupTabIcons() {

        View view1 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView1= view1.findViewById(R.id.image);
        imageView1.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_color));
        tabLayout.getTabAt(0).setCustomView(view1);

        View view2 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView2= view2.findViewById(R.id.image);
        imageView2.setImageDrawable(getResources().getDrawable(R.drawable.ic_liked_video_gray));
        tabLayout.getTabAt(1).setCustomView(view2);



        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){


            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v=tab.getCustomView();
                ImageView image=v.findViewById(R.id.image);

                switch (tab.getPosition()){
                    case 0:

                        if(UserVideo_F.myvideo_count>0){
                            create_popup_layout.setVisibility(View.GONE);
                        }else {
                            create_popup_layout.setVisibility(View.VISIBLE);
                            Animation aniRotate = AnimationUtils.loadAnimation(context,R.anim.up_and_down_animation);
                            create_popup_layout.startAnimation(aniRotate);
                        }

                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_color));
                        break;

                    case 1:
                        create_popup_layout.clearAnimation();
                        create_popup_layout.setVisibility(View.GONE);
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_liked_video_color));
                        break;
                }
                tab.setCustomView(v);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v=tab.getCustomView();
                ImageView image=v.findViewById(R.id.image);

                switch (tab.getPosition()){
                    case 0:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_gray));
                        break;
                    case 1:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_liked_video_gray));
                        break;
                }

                tab.setCustomView(v);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });


    }




    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final Resources resources;

        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();


        public ViewPagerAdapter(final Resources resources, FragmentManager fm) {
            super(fm);
            this.resources = resources;
        }

        @Override
        public Fragment getItem(int position) {
            final Fragment result;
            switch (position) {
                case 0:
                    result = new UserVideo_F(user_id);
                    break;
                case 1:
                    result = new Liked_Video_F(user_id);
                    break;

                default:
                    result = null;
                    break;
            }

            return result;
        }

        @Override
        public int getCount() {
            return 2;
        }



        @Override
        public CharSequence getPageTitle(final int position) {
            return null;
        }



        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }


        /**
         * Get the Fragment by position
         *
         * @param position tab position of the fragment
         * @return
         */
        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }


    }



    //this will get the all videos data of user and then parse the data
    private void Call_Api_For_get_Allvideos() {

        if(bundle==null){
            user_id=Variables.sharedPreferences.getString(Variables.u_id,"0");
        }

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("my_fb_id",Variables.sharedPreferences.getString(Variables.u_id,""));
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


        try {
            JSONObject jsonObject=new JSONObject(responce);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                JSONArray msgArray=jsonObject.getJSONArray("msg");

                JSONObject data=msgArray.getJSONObject(0);
                JSONObject user_info=data.optJSONObject("user_info");
                username.setText(user_info.optString("first_name")+" "+user_info.optString("last_name"));

                Profile_F.pic_url=user_info.optString("profile_pic");
                Picasso.with(context)
                        .load(Profile_F.pic_url)
                        .placeholder(context.getResources().getDrawable(R.drawable.profile_image_placeholder))
                        .resize(200,200).into(imageView);

                follow_count_txt.setText(data.optString("total_following"));
                fans_count_txt.setText(data.optString("total_fans"));
                heart_count_txt.setText(data.optString("total_heart"));


                if(!data.optString("fb_id").
                        equals(Variables.sharedPreferences.getString(Variables.u_id,""))) {

                    edit_profile_btn.setVisibility(View.VISIBLE);
                    JSONObject follow_Status = data.optJSONObject("follow_Status");
                    edit_profile_btn.setText(follow_Status.optString("follow_status_button"));
                    follow_status=follow_Status.optString("follow");
                }


                JSONArray user_videos=data.getJSONArray("user_videos");
                if(!user_videos.toString().equals("["+"0"+"]")){
                    video_count_txt.setText(user_videos.length()+" Videos");
                    create_popup_layout.setVisibility(View.GONE);

                }
                else {

                    create_popup_layout.setVisibility(View.VISIBLE);
                    Animation aniRotate = AnimationUtils.loadAnimation(context,R.anim.up_and_down_animation);
                    create_popup_layout.startAnimation(aniRotate);

                }


            }else {
                Toast.makeText(context, ""+jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }

    }




    public void Open_Setting(){


        if(!is_show_edit_profile){
            Open_Chat_F();
        }else {
            Open_menu_tab(setting_btn);
        }

    }



    public void Open_Edit_profile(){
        Edit_Profile_F edit_profile_f = new Edit_Profile_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, edit_profile_f).commit();
    }


    public  String follow_status="0";
    public void Follow_unFollow_User(){

        final String send_status;
        if(follow_status.equals("0")){
            send_status="1";
        }else {
            send_status="0";
        }

        Functions.Call_Api_For_Follow_or_unFollow(getActivity(),
                Variables.sharedPreferences.getString(Variables.u_id,""),
                user_id,
                send_status,
                new API_CallBack() {
                    @Override
                    public void ArrayData(ArrayList arrayList) {


                    }

                    @Override
                    public void OnSuccess(String responce) {

                        if(send_status.equals("1")){
                            edit_profile_btn.setText("UnFollow");
                            follow_status="1";

                        }
                        else if(send_status.equals("0")){
                            edit_profile_btn.setText("Follow");
                            follow_status="0";
                        }
                    }

                    @Override
                    public void OnFail(String responce) {

                    }

                });


    }



    //this method will get the big size of profile image.
    public void OpenfullsizeImage(String url){
        See_Full_Image_F see_image_f = new See_Full_Image_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        Bundle args = new Bundle();
        args.putSerializable("image_url", url);
        see_image_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, see_image_f).commit();
    }


    public void Open_menu_tab(View anchor_view){
        Context wrapper = new ContextThemeWrapper(context, R.style.AlertDialogCustom);
        PopupMenu popup = new PopupMenu(wrapper, anchor_view);
        popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popup.setGravity(Gravity.TOP|Gravity.RIGHT);
        }
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.edit_Profile_id:
                        Open_Edit_profile();
                        break;

                    case R.id.logout_id:
                        Logout();
                        break;

                }
                return true;
            }
        });

    }


    public void Open_Chat_F(){

        Chat_Activity chat_activity = new Chat_Activity();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("user_id", user_id);
        args.putString("user_name",user_name);
        args.putString("user_pic",user_pic);
        chat_activity.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, chat_activity).commit();


    }


    public void Open_Following(){

        Following_F following_f = new Following_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("id", user_id);
        args.putString("from_where","following");
        following_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, following_f).commit();

    }

    public void Open_Followers(){
        Following_F following_f = new Following_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("id", user_id);
        args.putString("from_where","fan");
        following_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, following_f).commit();

    }

    // this will erase all the user info store in locally and logout the user
    public void Logout(){
        SharedPreferences.Editor editor= Variables.sharedPreferences.edit();
        editor.putString(Variables.u_id,"");
        editor.putString(Variables.u_name,"");
        editor.putString(Variables.u_pic,"");
        editor.putBoolean(Variables.islogin,false);
        editor.commit();
        getActivity().finish();
        startActivity(new Intent(getActivity(), MainMenuActivity.class));
    }


    @Override
    public void onDetach() {
        super.onDetach();
        Functions.deleteCache(context);
    }


}
