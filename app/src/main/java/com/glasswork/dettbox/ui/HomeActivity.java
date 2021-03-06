package com.glasswork.dettbox.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.glasswork.dettbox.MainActivity;
import com.glasswork.dettbox.R;
import com.glasswork.dettbox.ui.home.HomeFragment;
import com.glasswork.dettbox.ui.profile.ProfileFragment;
import com.glasswork.dettbox.ui.ranking.MainRankingFragment;
import com.glasswork.dettbox.ui.tasks.MainTasksFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {

    private static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";

    public static final String DAY = "DAY";
    public static final String WEEK = "WEEK";
    public static final String MONTH = "MONTH";

    private String nfcTags;
    private String seasonEnded;

    private UsageStatsManager mUsageStatsManager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.DettboxTheme);
        setContentView(R.layout.activity_home);

        //sendNotificationSeasonEnded();

        saveIconsToLocal();
        setUserGroup();

        //saveTotalTimeToFirebase();
        String interval = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "HomeTimeSelected", MONTH);
        setMyAppsDataToFirebase(interval);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        sendToFragment(savedInstanceState);
    }

    public void sendToFragment(Bundle savedInstanceState) {
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
            /*prefs.edit().remove("eSyllabus").commit();*/
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
                case "FMWhatsApp":
                case "YoWhatsApp":
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Date atStartOfDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setMyAppsDataToFirebase(String mode) {
        for (int i = 0; i < 3; i++) {
            Calendar cal = Calendar.getInstance();
            String timeInterval = null;
            switch (i) {
                case 0:
                    cal.setTime(atStartOfDay(Calendar.getInstance().getTime()));
                    timeInterval = DAY;
                    break;

                case 1:
                    cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
                    cal.clear(Calendar.MINUTE);
                    cal.clear(Calendar.SECOND);
                    cal.clear(Calendar.MILLISECOND);
                    cal.setFirstDayOfWeek(Calendar.MONDAY);
                    cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                    timeInterval = WEEK;
                    break;

                case 2:
                    cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
                    cal.clear(Calendar.MINUTE);
                    cal.clear(Calendar.SECOND);
                    cal.clear(Calendar.MILLISECOND);
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    timeInterval = MONTH;
                    break;
            }

            getSingleAppData(getApplicationContext(), "com.whatsapp", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "edu.salleurl.esyllabus", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "com.glasswork.dettbox", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "com.instagram.android", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "com.netflix.mediaclient", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "org.telegram.messenger", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "com.discord", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "com.twitter.android", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "tv.twitch.android.app", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "com.google.android.youtube", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "com.zhiliaoapp.musically", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "com.snapchat.android", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "com.facebook.katana", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "com.facebook.orca", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "com.reddit.frontpage", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "com.bereal.ft", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "com.vanced.android.youtube", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "com.fmwhatsapp", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "com.yowhatsapp", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
            getSingleAppData(getApplicationContext(), "com.tinder", cal.getTimeInMillis(), System.currentTimeMillis(), timeInterval);
        }
    }

    public void getSingleAppData(Context context, String packageName, long startSeason, long actualTime, String timeInterval) {

        mUsageStatsManager = (UsageStatsManager)getApplicationContext().getSystemService(context.USAGE_STATS_SERVICE);
        String savedStateMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "HomeTimeSelected", MONTH);
        Map<String, UsageStats> queryUsageStats = mUsageStatsManager.queryAndAggregateUsageStats(startSeason, actualTime);
        UsageStats usageStats;
        String appName = getAppName(packageName);

        if (queryUsageStats.containsKey(packageName)) {
            usageStats = queryUsageStats.get(packageName);
        } else {
            switch (timeInterval) {
                case DAY:
                    FirebaseDatabase.getInstance(FIREBASE_LINK)
                            .getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("Apps")
                            .child(packageName.replace(".", "-"))
                            .child("timeDaily")
                            .setValue("00h 00m");
                    break;
                case WEEK:
                    FirebaseDatabase.getInstance(FIREBASE_LINK)
                            .getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("Apps")
                            .child(packageName.replace(".", "-"))
                            .child("timeWeekly")
                            .setValue("00h 00m");
                    break;
                case MONTH:
                    FirebaseDatabase.getInstance(FIREBASE_LINK)
                            .getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("Apps")
                            .child(packageName.replace(".", "-"))
                            .child("time")
                            .setValue("00h 00m");
                    break;
            }
            FirebaseDatabase.getInstance(FIREBASE_LINK)
                    .getReference("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("Apps")
                    .child(packageName.replace(".", "-"))
                    .child("name")
                    .setValue(appName);
            return;
        }
        /*Log.e("DAAAAAAAAAAAAAAYYY", "NAME: " + usageStats.getPackageName() + "UsageStatsAdapter: " + convertMillisToHourMinute(usageStats.getTotalTimeInForeground()));*/

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            switch (timeInterval) {
                case DAY:
                    FirebaseDatabase.getInstance(FIREBASE_LINK)
                            .getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("Apps")
                            .child(packageName.replace(".", "-"))
                            .child("timeDaily")
                            .setValue(convertMillisToHourMinute(usageStats.getTotalTimeInForeground()));
                    break;
                case WEEK:
                    FirebaseDatabase.getInstance(FIREBASE_LINK)
                            .getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("Apps")
                            .child(packageName.replace(".", "-"))
                            .child("timeWeekly")
                            .setValue(convertMillisToHourMinute(usageStats.getTotalTimeInForeground()));
                    break;
                case MONTH:
                    FirebaseDatabase.getInstance(FIREBASE_LINK)
                            .getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("Apps")
                            .child(packageName.replace(".", "-"))
                            .child("time")
                            .setValue(convertMillisToHourMinute(usageStats.getTotalTimeInForeground()));
                    break;
            }
            FirebaseDatabase.getInstance(FIREBASE_LINK)
                    .getReference("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("Apps")
                    .child(packageName.replace(".", "-"))
                    .child("name")
                    .setValue(appName);
        }

    }

    private String convertMillisToHourMinute(long lastTimeUsed) {
        String format = "%02dh %02dm";
        return String.format(
                format,
                TimeUnit.MILLISECONDS.toHours(lastTimeUsed),
                TimeUnit.MILLISECONDS.toMinutes(lastTimeUsed) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(lastTimeUsed)
                )
        );
    }

    public int convertHourMinuteToInt(String timeString) {
        String[] aux = timeString.split("h ", 2);
        String hours = aux[0];
        String[] aux2 = aux[1].split("m", 2);
        String mins = aux2[0];
        return Integer.parseInt(hours)*60*60 + Integer.parseInt(mins)*60;
    }

    private String getAppName(String packageName) {
        String appName;
        switch (packageName) {
            case "com.whatsapp":
                appName = "WhatsApp";
                break;
            case "deezer.android.app":
                appName = "Deezer";
                break;
            case "com.glasswork.dettbox":
                appName = "Dettbox";
                break;
            case "com.instagram.android":
                appName = "Instagram";
                break;
            case "com.netflix.mediaclient":
                appName = "Netflix";
                break;
            case "org.telegram.messenger":
                appName = "Telegram";
                break;
            case "com.discord":
                appName = "Discord";
                break;
            case "com.twitter.android":
                appName = "Twitter";
                break;
            case "tv.twitch.android.app":
                appName = "Twitch";
                break;
            case "com.google.android.youtube":
                appName = "YouTube";
                break;
            case "com.zhiliaoapp.musically":
                appName = "TikTok";
                break;
            case "com.snapchat.android":
                appName = "Snapchat";
                break;
            case "com.tinder":
                appName = "Tinder";
                break;
            case "com.facebook.katana":
                appName = "Facebook";
                break;
            case "edu.salleurl.esyllabus":
                appName = "eSyllabus";
                break;
            case "com.facebook.orca":
                appName = "Messenger";
                break;
            case "com.reddit.frontpage":
                appName = "Reddit";
                break;
            case "com.bereal.ft":
                appName = "BeReal";
                break;
            case "com.vanced.android.youtube":
                appName = "YouTube Vanced";
                break;
            case "com.fmwhatsapp":
                appName = "FMWhatsApp";
                break;
            case "com.yowhatsapp":
                appName = "YoWhatsApp";
                break;
            default:
                appName = "App not found";
        }
        return appName;
    }

    public void sendNotificationSeasonEnded() {

        String prefsGroupName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");

        FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Groups")
                .child(prefsGroupName)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.hasChild("nfcTags")&& snapshot.hasChild("seasonEnded")) {
                            nfcTags = snapshot.child("nfcTags").getValue().toString();
                            seasonEnded = snapshot.child("seasonEnded").getValue().toString();
                            Log.e("HAHAHJSHAJHAJS", "onDataChange: " +snapshot.child("nfcTags").getValue().toString());
                            if (nfcTags.equals("true") && seasonEnded.equals("true")) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    NotificationChannel channel = new NotificationChannel("My Notification", "My Notification", NotificationManager.IMPORTANCE_DEFAULT);
                                    NotificationManager manager = getSystemService(NotificationManager.class);
                                    manager.createNotificationChannel(channel);
                                }

                                String message = "Ve a ver los resultados finales en los cubos";
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(HomeActivity.this, "My Notification")
                                        .setContentTitle("Temporada Finalizada!")
                                        .setContentText(message)
                                        .setSmallIcon(R.drawable.ic_baseline_message_24)
                                        .setAutoCancel(true);

                                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(HomeActivity.this);
                                managerCompat.notify(1,builder.build());
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




    }



}