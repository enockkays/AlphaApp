package com.me.alpha.Following;

import android.annotation.SuppressLint;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.me.alpha.Profile.Profile_F;
import com.me.alpha.R;
import com.me.alpha.SimpleClasses.API_CallBack;
import com.me.alpha.SimpleClasses.Functions;
import com.me.alpha.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Following_F extends Fragment {

    View view;
    Context context;

    String user_id;


    Following_Adapter adapter;
    RecyclerView recyclerView;

    ArrayList<Following_Get_Set> datalist;


    RelativeLayout no_data_layout;

    ProgressBar pbar;

    String following_or_fan="Followers";

    TextView title_txt;
    public Following_F() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_following, container, false);
        context=getContext();

        Bundle bundle=getArguments();
        if(bundle!=null){
            user_id=bundle.getString("id");
            following_or_fan=bundle.getString("from_where");
        }


        title_txt=view.findViewById(R.id.title_txt);

        datalist=new ArrayList<>();

        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);



        adapter=new Following_Adapter(context, following_or_fan,datalist, new Following_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, Following_Get_Set item) {

                switch (view.getId()){
                    case R.id.action_txt:
                        Follow_unFollow_User(item,postion);
                        break;

                    case R.id.mainlayout:
                        OpenProfile(item);
                        break;

                }

            }
        }
        );

        recyclerView.setAdapter(adapter);


        no_data_layout=view.findViewById(R.id.no_data_layout);
        pbar=view.findViewById(R.id.pbar);



        view.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });



        if(following_or_fan.equals("following")){
            Call_Api_For_get_Allfollowing();
            title_txt.setText("Following");
        }
        else {
            Call_Api_For_get_Allfan();
            title_txt.setText("Followers");
        }

        return view;
    }


    // Bottom two function will call the api and get all the videos form api and parse the json data
    private void Call_Api_For_get_Allfollowing() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id",user_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        Log.d("resp",parameters.toString());
        Log.d("resp-home", Variables.get_followings);

        RequestQueue rq = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, Variables.get_followings, parameters, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String respo=response.toString();
                        Log.d("resp-home",respo);
                        Parse_following_data(respo);
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

    public void Parse_following_data(String responce){

        datalist.clear();

        try {
            JSONObject jsonObject=new JSONObject(responce);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                JSONArray msgArray=jsonObject.getJSONArray("msg");
                for (int i=0;i<msgArray.length();i++) {
                    JSONObject profile_data = msgArray.optJSONObject(i);

                    JSONObject follow_Status=profile_data.optJSONObject("follow_Status");

                    Following_Get_Set item=new Following_Get_Set();
                    item.fb_id=profile_data.optString("fb_id");
                    item.first_name=profile_data.optString("first_name");
                    item.last_name=profile_data.optString("last_name");
                    item.bio=profile_data.optString("bio");
                    item.username=profile_data.optString("username");
                    item.profile_pic=profile_data.optString("profile_pic");


                    item.follow=follow_Status.optString("follow");
                    item.follow_status_button=follow_Status.optString("follow_status_button");


                    datalist.add(item);
                    adapter.notifyItemInserted(i);
                }

                adapter.notifyDataSetChanged();

                pbar.setVisibility(View.GONE);

                if(datalist.isEmpty()){
                    no_data_layout.setVisibility(View.VISIBLE);
                }else
                    no_data_layout.setVisibility(View.GONE);

            }else {
                Toast.makeText(context, ""+jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }

    }




    // Bottom two function will call the api and get all the videos form api and parse the json data
    private void Call_Api_For_get_Allfan() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id",user_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        Log.d("resp",parameters.toString());
        Log.d("resp-home",Variables.get_followers);

        RequestQueue rq = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, Variables.get_followers, parameters, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String respo=response.toString();
                        Log.d("resp-home",respo);
                        Parse_fans_data(respo);
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

    public void Parse_fans_data(String responce){

        datalist.clear();

        try {
            JSONObject jsonObject=new JSONObject(responce);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                JSONArray msgArray=jsonObject.getJSONArray("msg");
                for (int i=0;i<msgArray.length();i++) {
                    JSONObject profile_data = msgArray.optJSONObject(i);

                    JSONObject follow_Status=profile_data.optJSONObject("follow_Status");

                    Following_Get_Set item=new Following_Get_Set();
                    item.fb_id=profile_data.optString("fb_id");
                    item.first_name=profile_data.optString("first_name");
                    item.last_name=profile_data.optString("last_name");
                    item.bio=profile_data.optString("bio");
                    item.username=profile_data.optString("username");
                    item.profile_pic=profile_data.optString("profile_pic");


                    item.follow=follow_Status.optString("follow");
                    item.follow_status_button=follow_Status.optString("follow_status_button");


                    datalist.add(item);
                    adapter.notifyItemInserted(i);
                }

                adapter.notifyDataSetChanged();

                pbar.setVisibility(View.GONE);

                if(datalist.isEmpty()){
                    no_data_layout.setVisibility(View.VISIBLE);
                }else
                    no_data_layout.setVisibility(View.GONE);

            }else {
                Toast.makeText(context, ""+jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }

    }



    // this will open the profile of user which have uploaded the currenlty running video
    @SuppressLint("ResourceType")
    private void OpenProfile(final Following_Get_Set item) {
        Profile_F profile_f = new Profile_F(false);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        Bundle args = new Bundle();
        args.putString("user_id",item.fb_id);
        args.putString("user_name",item.first_name+" "+item.last_name);
        args.putString("user_pic",item.profile_pic);
        profile_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, profile_f).commit();
    }


    public void Follow_unFollow_User(final Following_Get_Set item, final int position){

        final String send_status;
        if(item.follow.equals("0")){
            send_status="1";
        }else {
            send_status="0";
        }

        Functions.Call_Api_For_Follow_or_unFollow(getActivity(),
                Variables.sharedPreferences.getString(Variables.u_id,""),
                item.fb_id,
                send_status,
                new API_CallBack() {
                    @Override
                    public void ArrayData(ArrayList arrayList) {


                    }

                    @Override
                    public void OnSuccess(String responce) {

                        if(send_status.equals("1")){
                            item.follow="1";
                            datalist.remove(position);
                            datalist.add(position,item);
                        }
                        else if(send_status.equals("0")){
                            item.follow="0";
                            datalist.remove(position);
                            datalist.add(position,item);
                        }

                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void OnFail(String responce) {

                    }

                });


    }




}
