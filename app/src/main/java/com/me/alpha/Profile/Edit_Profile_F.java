package com.me.alpha.Profile;

import android.Manifest;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.me.alpha.SimpleClasses.API_CallBack;
import com.me.alpha.SimpleClasses.Functions;
import com.me.alpha.SimpleClasses.Variables;
import com.me.alpha.Main_Menu.RelateOnFragment_OnBack.RootFragment;
import com.me.alpha.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.me.alpha.Main_Menu.MainMenuFragment.hasPermissions;

public class Edit_Profile_F extends RootFragment implements View.OnClickListener {

    View view;
    Context context;

    public Edit_Profile_F() {

    }

    ImageView profile_image;
    EditText firstname_edit,lastname_edit,user_bio_edit;

    RadioButton male_btn,female_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_edit_profile, container, false);
        context=getContext();


        view.findViewById(R.id.Goback).setOnClickListener(this);
        view.findViewById(R.id.save_btn).setOnClickListener(this);
        view.findViewById(R.id.upload_pic_btn).setOnClickListener(this);



        profile_image=view.findViewById(R.id.profile_image);
        firstname_edit=view.findViewById(R.id.firstname_edit);
        lastname_edit=view.findViewById(R.id.lastname_edit);
        user_bio_edit=view.findViewById(R.id.user_bio_edit);


        firstname_edit.setText(Variables.sharedPreferences.getString(Variables.f_name,""));
        lastname_edit.setText(Variables.sharedPreferences.getString(Variables.l_name,""));

        Picasso.with(context)
                .load(Variables.sharedPreferences.getString(Variables.u_pic,""))
                .placeholder(R.drawable.profile_image_placeholder)
                .into(profile_image);


        male_btn=view.findViewById(R.id.male_btn);
        female_btn=view.findViewById(R.id.female_btn);



        Call_Api_For_User_Details();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.Goback:
                getActivity().onBackPressed();
                break;

            case R.id.save_btn:
                if(Check_Validation()){

                    Call_Api_For_Edit_profile();
                }
                break;

            case R.id.upload_pic_btn:
                selectImage();
                break;
        }
    }



    // this method will show the dialog of selete the either take a picture form camera or pick the image from gallary
    private void selectImage() {

        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };



        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.AlertDialogCustom);

        builder.setTitle("Add Photo!");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo"))

                {
                    if(check_permissions())
                        openCameraIntent();

                }

                else if (options[item].equals("Choose from Gallery"))

                {

                    if(check_permissions()) {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, 2);
                    }
                }

                else if (options[item].equals("Cancel")) {

                    dialog.dismiss();

                }

            }

        });

        builder.show();

    }


    public boolean check_permissions() {

        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };

        if (!hasPermissions(context, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, 2);
        }else {

            return true;
        }

        return false;
    }




    // below three method is related with taking the picture from camera
    private void openCameraIntent() {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if(pictureIntent.resolveActivity(getActivity().getPackageManager()) != null){
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context.getApplicationContext(), Variables.file_provider_path, photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(pictureIntent, 1);
            }
        }
    }

    String imageFilePath;
    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }

    public  String getPath(Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 1) {
                Matrix matrix = new Matrix();
                try {
                    ExifInterface exif = new ExifInterface(imageFilePath);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            matrix.postRotate(90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            matrix.postRotate(180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            matrix.postRotate(270);
                            break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Uri selectedImage =(Uri.fromFile(new File(imageFilePath)));

                InputStream imageStream = null;
                try {
                    imageStream =getActivity().getContentResolver().openInputStream(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);
                Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);


            }

            else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                InputStream imageStream = null;
                try {
                    imageStream =getActivity().getContentResolver().openInputStream(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);

                String path=getPath(selectedImage);
                Matrix matrix = new Matrix();
                ExifInterface exif = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    try {
                        exif = new ExifInterface(path);
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                        switch (orientation) {
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                matrix.postRotate(90);
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                matrix.postRotate(180);
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_270:
                                matrix.postRotate(270);
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);

            }

        }

    }



    // this will check the validations like none of the field can be the empty
    public boolean Check_Validation(){
        String firstname=firstname_edit.getText().toString();
        String lastname=lastname_edit.getText().toString();

        if(TextUtils.isEmpty(firstname)){
            return false;
        }
        else if(TextUtils.isEmpty(lastname)){
            return false;
        }

        return true;
    }



    // this will update the latest info of user in database
    public  void Call_Api_For_Edit_profile() {

        Functions.Show_loader(context,false,false);

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id,"0"));
            parameters.put("first_name",firstname_edit.getText().toString());
            parameters.put("last_name",lastname_edit.getText().toString());

            if(male_btn.isChecked()){
                parameters.put("gender","Male");

            }else if(female_btn.isChecked()){
                parameters.put("gender","Female");
            }

            parameters.put("bio",user_bio_edit.getText().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("resp",parameters.toString());

        RequestQueue rq = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, Variables.edit_profile, parameters, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Functions.cancel_loader();

                        Log.d("responce", response.toString());
                        String code=response.optString("code");
                        if(code.equals("200")){

                            SharedPreferences.Editor editor =Variables.sharedPreferences.edit();

                            editor.putString(Variables.f_name,firstname_edit.getText().toString());
                            editor.putString(Variables.l_name,lastname_edit.getText().toString());
                            editor.commit();


                            getActivity().onBackPressed();

                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Functions.cancel_loader();
                        Toast.makeText(context, "Something wrong with Api", Toast.LENGTH_SHORT).show();
                        Log.d("respo",error.toString());
                    }
                });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rq.getCache().clear();
        rq.add(jsonObjectRequest);

    }





    // this will get the user data and parse the data and show the data into views
    public void Call_Api_For_User_Details(){

        Functions.Call_Api_For_Get_User_data(getActivity(),
                Variables.sharedPreferences.getString(Variables.u_id, ""),
                new API_CallBack() {
                    @Override
                    public void ArrayData(ArrayList arrayList) {

                    }

                    @Override
                    public void OnSuccess(String responce) {
                        Parse_user_data(responce);
                    }

                    @Override
                    public void OnFail(String responce) {

                    }
                });
    }

    public void Parse_user_data(String responce){
        try {
            JSONObject jsonObject=new JSONObject(responce);
            JSONArray msg=jsonObject.optJSONArray("msg");
            JSONObject data=msg.getJSONObject(0);

            firstname_edit.setText(data.optString("first_name"));
            lastname_edit.setText(data.optString("last_name"));

            String picture=data.optString("profile_pic");
            Picasso.with(context)
                    .load(picture)
                    .placeholder(R.drawable.profile_image_placeholder)
                    .into(profile_image);

            String gender=data.optString("gender");
            if(gender.equals("Male")){
                male_btn.setChecked(true);
            }else {
                female_btn.setChecked(true);
            }

            user_bio_edit.setText(data.optString("bio"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
