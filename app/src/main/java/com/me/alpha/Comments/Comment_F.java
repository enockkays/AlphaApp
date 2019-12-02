package com.me.alpha.Comments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.me.alpha.SimpleClasses.API_CallBack;
import com.me.alpha.SimpleClasses.Fragment_Data_Send;
import com.me.alpha.SimpleClasses.Functions;
import com.me.alpha.SimpleClasses.Variables;
import com.me.alpha.Main_Menu.RelateOnFragment_OnBack.RootFragment;
import com.me.alpha.R;

import java.util.ArrayList;

public class Comment_F extends RootFragment {

    View view;
    Context context;

    RecyclerView recyclerView;

    Comments_Adapter adapter;

    ArrayList<Comment_Get_Set> data_list;

    String video_id;

    EditText message_edit;
    ImageButton send_btn;
    ProgressBar send_progress;

    TextView comment_count_txt;

    FrameLayout comment_screen;

    public static int comment_count=0;
    public Comment_F() {

    }

    Fragment_Data_Send fragment_data_send;
    @SuppressLint("ValidFragment")
    public Comment_F(int count, Fragment_Data_Send fragment_data_send){
        comment_count=count;
        this.fragment_data_send=fragment_data_send;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_comment, container, false);
        context=getContext();

        view.findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().onBackPressed();
            }
        });


        Bundle bundle=getArguments();
        if(bundle!=null){
            video_id=bundle.getString("video_id");
        }


        comment_count_txt=view.findViewById(R.id.comment_count);

        recyclerView=view.findViewById(R.id.recylerview);
        LinearLayoutManager layoutManager=new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);


        data_list=new ArrayList<>();
        adapter=new Comments_Adapter(context, data_list, new Comments_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, Comment_Get_Set item, View view) {


            }
        });

        recyclerView.setAdapter(adapter);


        message_edit=view.findViewById(R.id.message_edit);


        send_progress=view.findViewById(R.id.send_progress);
        send_btn=view.findViewById(R.id.send_btn);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message=message_edit.getText().toString();

                if(!TextUtils.isEmpty(message)){
                    if(Variables.sharedPreferences.getBoolean(Variables.islogin,false)){
                        Send_Comments(video_id,message);
                        message_edit.setText(null);
                        send_progress.setVisibility(View.VISIBLE);
                        send_btn.setVisibility(View.GONE);
                    }
                    else {
                        Toast.makeText(context, "Please Login into the app", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });


        comment_screen=view.findViewById(R.id.comment_screen);
        comment_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().onBackPressed();

            }
        });

        Get_All_Comments();


        return view;
    }




    // this funtion will get all the comments against post
    public void Get_All_Comments(){

        Functions.Call_Api_For_get_Comment(getActivity(), video_id, new API_CallBack() {
            @Override
            public void ArrayData(ArrayList arrayList) {
                ArrayList<Comment_Get_Set> arrayList1=arrayList;
                for(Comment_Get_Set item:arrayList1){
                    data_list.add(item);
                }
                comment_count_txt.setText(data_list.size()+" comments");
                adapter.notifyDataSetChanged();
            }

            @Override
            public void OnSuccess(String responce) {

            }

            @Override
            public void OnFail(String responce) {

            }

        });

    }




    // this function will call an api to upload your comment
    public void Send_Comments(String video_id,String comment){

        Functions.Call_Api_For_Send_Comment(getActivity(), video_id,comment ,new API_CallBack() {
            @Override
            public void ArrayData(ArrayList arrayList) {
                ArrayList<Comment_Get_Set> arrayList1=arrayList;
                for(Comment_Get_Set item:arrayList1){
                    data_list.add(0,item);
                    comment_count++;

                    comment_count_txt.setText(comment_count+" comments");

                    if(fragment_data_send!=null)
                        fragment_data_send.onDataSent(""+comment_count);

                }
                adapter.notifyDataSetChanged();
                send_progress.setVisibility(View.GONE);
                send_btn.setVisibility(View.VISIBLE);
            }

            @Override
            public void OnSuccess(String responce) {

            }

            @Override
            public void OnFail(String responce) {

            }
        });

    }



}
