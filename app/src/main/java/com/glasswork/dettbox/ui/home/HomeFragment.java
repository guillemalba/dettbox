package com.glasswork.dettbox.ui.home;

import android.annotation.SuppressLint;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.pm.PackageInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glasswork.dettbox.MyService;
import com.glasswork.dettbox.R;
import com.glasswork.dettbox.RegisterActivity;
import com.glasswork.dettbox.model.AppItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    private ArrayList<AppItem> appItemList;
    private RecyclerView recyclerView;
    private RecyclerAdapter myAdapter;

    private LinkedList<PackageInfo> listPackInfo;
    private UsageStatsManager mUsageStatsManager;
    private PackageManager mPm;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // to remove top navbar
        /*getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().setNavigationBarColor(getResources().getColor(R.color.colorDark));*/

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        appItemList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recycle_app_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        myAdapter = new RecyclerAdapter(appItemList);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        cal.set(Calendar.DAY_OF_MONTH, 1);

        getAppData(getContext(), "com.whatsapp", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.tinder", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.glasswork.dettbox", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.instagram.android", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.netflix.mediaclient", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "org.telegram.messenger", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.discord", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.twitter.android", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "tv.twitch.android.app", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.google.android.youtube", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.zhiliaoapp.musically", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.snapchat.android", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.tinder", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.facebook.katana", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.facebook.orca", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.reddit.frontpage", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.bereal.ft", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.vanced.android.youtube", cal.getTimeInMillis(), System.currentTimeMillis());

        setAdapter();
        setAppInfo();
        saveTotalTimeToFirebase();

        return view;
    }

    public void getAppData(Context context, String packageName, long startSeason, long actualTime) {

        mUsageStatsManager = (UsageStatsManager)getContext().getSystemService(context.USAGE_STATS_SERVICE);
        final List<UsageStats> stats =
                mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                        startSeason, actualTime);
        if (stats == null) {
            return;
        }

        ArrayMap<String, UsageStats> map = new ArrayMap<>();
        final int statCount = stats.size();
        for (int i = 0; i < statCount; i++) {

            final android.app.usage.UsageStats pkgStats = stats.get(i);

            if (pkgStats.getPackageName().equals(packageName)) {
                // load application labels for each application

                String appName = getAppName(packageName);

                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    FirebaseDatabase.getInstance(FIREBASE_LINK)
                            .getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("Apps")
                            .child(packageName.replace(".", "-"))
                            .child("time")
                            .setValue(convertTimeToString(pkgStats.getTotalTimeInForeground()));

                    FirebaseDatabase.getInstance(FIREBASE_LINK)
                            .getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("Apps")
                            .child(packageName.replace(".", "-"))
                            .child("name")
                            .setValue(appName);
                }
            }
        }
    }

    public int convertTimeToSeconds(String timeString) {
        String[] aux = timeString.split("h ", 2);
        String hours = aux[0];
        String[] aux2 = aux[1].split("m ", 2);
        String mins = aux2[0];
        String[] aux3 = aux2[1].split("s", 2);
        String sec = aux3[0];
        return Integer.parseInt(hours)*60*60 + Integer.parseInt(mins)*60 + Integer.parseInt(sec);
    }

    void saveTotalTimeToFirebase () {
        //readUser();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                    .getReference("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("Apps");

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int totalTime = 0;
                        for (DataSnapshot dataSnapshot:snapshot.getChildren()) {
                            String time = dataSnapshot.child("time").getValue().toString();

                            totalTime += convertTimeToSeconds(time);

                        }
                        // boolean if player is in the group or not to save its info
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                        Boolean yourLocked = prefs.getBoolean(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupStatus", true);
                        String prefsGroupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");
                        if (!yourLocked) {
                            if (!prefsGroupName.equals("null")) {
                                String timeString = convertTimeToString(totalTime*1000L);
                                FirebaseDatabase.getInstance(FIREBASE_LINK)
                                        .getReference("Groups")
                                        .child(prefsGroupName)
                                        .child("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("totalMinutes")
                                        .setValue(timeString);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
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
            default:
                appName = "App not found";
        }
        return appName;
    }

    private String convertTimeToString(long lastTimeUsed) {
        return String.format(
                "%02dh %02dm %02ds",
                TimeUnit.MILLISECONDS.toHours(lastTimeUsed),
                TimeUnit.MILLISECONDS.toMinutes(lastTimeUsed) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(lastTimeUsed)
                ),
                TimeUnit.MILLISECONDS.toSeconds(lastTimeUsed) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(lastTimeUsed)
                )
        );
    }

    private int appExist(ArrayList<AppItem> list, String appName) {
        int position = -2;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getAppName().equals(appName)) {
                return i;
            } else {
                position = -1;
            }
        }
        return position;
    }

    private void setAdapter() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(myAdapter);
    }

    private void setAppInfo() {

        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Apps");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        String time = dataSnapshot.child("time").getValue().toString();

                        Drawable drawableIcon = getActivity().getResources().getDrawable(R.drawable.no_icon);
                        if (name.equals("YouTube Vanced")) {
                            drawableIcon = getActivity().getResources().getDrawable(R.drawable.ic_youtube_svgrepo_com);
                        }

                        if (!time.equals("00h 00m 00s")) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                            String json = prefs.getString(name, null);
                            if (json != null) {
                                PackageInfo packInfo = gson.fromJson(json, PackageInfo.class);
                                String appName = packInfo.applicationInfo.loadLabel(getActivity().getPackageManager()).toString();
                                if (appName.equals(name)) {
                                    drawableIcon = packInfo.applicationInfo.loadIcon(getActivity().getPackageManager());
                                }
                            }

                            AppItem appItem = new AppItem(name, time, drawableIcon);
                            // if app already exist, update it
                            int position = appExist(appItemList, name);
                            if (position >= 0) {
                                appItemList.get(position).setAppTimeUsed(time);
                            } else {
                                appItemList.add(appItem);
                            }
                        }

                    }
                    // sort the appList before print to screen
                    Collections.sort(appItemList, new Comparator<AppItem>(){
                        public int compare(AppItem obj1, AppItem obj2) {
                            // ## Ascending order
                            /*return obj1.getAppName().compareToIgnoreCase(obj2.getAppName());*/
                            return obj2.getAppTimeUsed().compareToIgnoreCase(obj1.getAppTimeUsed());
                            // return Integer.valueOf(obj1.empId).compareTo(Integer.valueOf(obj2.empId)); // To compare integer values

                            // ## Descending order
                            // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                            // return Integer.valueOf(obj2.empId).compareTo(Integer.valueOf(obj1.empId)); // To compare integer values
                        }
                    });
                    myAdapter.notifyDataSetChanged();

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}
