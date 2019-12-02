package com.me.alpha.Main_Menu;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.me.alpha.SimpleClasses.Variables;
import com.me.alpha.R;

public class MainMenuActivity extends AppCompatActivity {
    private MainMenuFragment mainMenuFragment;
    long mBackPressed;



    public  static  MainMenuActivity mainMenuActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Variables.screen_height= displayMetrics.heightPixels;
        Variables.screen_width= displayMetrics.widthPixels;

        Variables.sharedPreferences=getSharedPreferences(Variables.pref_name,MODE_PRIVATE);

        Variables.user_id=Variables.sharedPreferences.getString(Variables.u_id,"");
        Variables.user_name=Variables.sharedPreferences.getString(Variables.u_name,"");
        Variables.user_pic=Variables.sharedPreferences.getString(Variables.u_pic,"");

        if (savedInstanceState == null) {

            initScreen();

        } else {
            mainMenuFragment = (MainMenuFragment) getSupportFragmentManager().getFragments().get(0);
        }


        // get version of currently running app
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


    }


    private void initScreen() {
        mainMenuFragment = new MainMenuFragment();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mainMenuFragment)
                .commit();

        findViewById(R.id.container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Normal event dispatch to this container's children, ignore the return value

        if (getCurrentFocus() != null) {
            try{
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
            catch (Exception e){

            }
        }
        return super.dispatchTouchEvent(ev);

    }


    @Override
    public void onBackPressed() {
        if (!mainMenuFragment.onBackPressed()) {
            int count = this.getSupportFragmentManager().getBackStackEntryCount();
            if (count == 0) {
                if (mBackPressed + 2000 > System.currentTimeMillis()) {
                    super.onBackPressed();
                    return;
                } else {
                    Toast.makeText(getBaseContext(), "Tap Again To Exit", Toast.LENGTH_SHORT).show();
                    mBackPressed = System.currentTimeMillis();

                }
            } else {
                super.onBackPressed();
            }
        }

    }




}
