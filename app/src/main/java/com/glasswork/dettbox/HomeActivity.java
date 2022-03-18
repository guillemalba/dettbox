package com.glasswork.dettbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import com.glasswork.dettbox.model.User;
import com.glasswork.dettbox.ui.home.HomeFragment;
import com.glasswork.dettbox.ui.profile.ProfileFragment;
import com.glasswork.dettbox.ui.ranking.MainRankingFragment;
import com.glasswork.dettbox.ui.tasks.MainTasksFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    Intent mServiceIntent;
    /*private MyService mYourService;*/
    private static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.DettboxTheme);
        /*setTheme(R.style.Theme_AppCompat);*/
        setContentView(R.layout.activity_home);

        /*setTheme(R.style.Theme_AppCompat);*/ //TODO
        // to remove top navbar
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorDark));*/

        saveIconsToLocal();
        setUserGroup();

        /*mYourService = new MyService();
        mServiceIntent = new Intent(this, mYourService.getClass());
        if (!isMyServiceRunning(mYourService.getClass())) {
            startService(mServiceIntent); // TODO: to start background service
        }*/

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //FrameLayout frameLayout = findViewById(R.id.fragment_container);

        // line to initialize the first HomeFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit(); // TODO: temporal here goes HomeFragment
        }


    }

    public void setUserGroup() {
        FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String groupName = snapshot.child("groupName").getValue().toString();
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            prefs.edit().putString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", groupName).commit();
                            prefs.edit().putBoolean(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupStatus", false).commit();
                            if (groupName.equals("null")) {
                                prefs.edit().putBoolean(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupStatus", true).commit();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public void saveIconsToLocal() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        List<PackageInfo> packList = getPackageManager().getInstalledPackages(0);
        for (int i=0; i < packList.size(); i++)
        {
            PackageInfo packInfo = packList.get(i);
            String appName = packInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            /*prefs.edit().remove("Tinder").commit();*/
            switch (appName) {
                case "WhatsApp":
                case "Deezer":
                case "Dettbox":
                case "Instagram":
                case "Netflix":
                case "Telegram":
                case "Discord":
                case "Twitter":
                case "Twitch":
                case "YouTube":
                case "TikTok":
                case "Facebook":
                case "Messenger":
                case "Tinder":
                /*case "eSyllabus":*/
                case "Snapchat":
                case "BeReal":
                case "Reddit":
                    String json = gson.toJson(packList.get(i));
                    prefs.edit().putString(appName, json).commit();
                    break;
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    //FrameLayout frameLayout = findViewById(R.id.fragment_container);

                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_ranking:
                            selectedFragment = new MainRankingFragment();
                            break;
                        case R.id.nav_tasks:
                            selectedFragment = new MainTasksFragment();
                            break;
                        case R.id.nav_profile:
                            selectedFragment = new ProfileFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                    return true;
                }
            };

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }

    @Override
    protected void onDestroy() {
        /*//stopService(mServiceIntent);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);*/
        super.onDestroy();
    }

}