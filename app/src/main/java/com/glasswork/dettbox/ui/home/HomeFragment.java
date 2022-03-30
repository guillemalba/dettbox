package com.glasswork.dettbox.ui.home;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.pm.PackageInfo;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glasswork.dettbox.MainActivity;
import com.glasswork.dettbox.Messages;
import com.glasswork.dettbox.R;
import com.glasswork.dettbox.model.AppItem;
import com.glasswork.dettbox.model.UserRanking;
import com.glasswork.dettbox.ui.HomeActivity;
import com.glasswork.dettbox.ui.ranking.MainRankingFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
    public static final String DAY = "DAY";
    public static final String WEEK = "WEEK";
    public static final String MONTH = "MONTH";

    private Button btnDay;
    private Button btnWeek;
    private Button btnMonth;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // to remove top navbar
        /*getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().setNavigationBarColor(getResources().getColor(R.color.colorDark));*/

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnDay = getActivity().findViewById(R.id.button_day);
        btnWeek = getActivity().findViewById(R.id.button_week);
        btnMonth = getActivity().findViewById(R.id.button_month);

        btnDay.setVisibility(View.VISIBLE);
        btnWeek.setVisibility(View.VISIBLE);
        btnMonth.setVisibility(View.VISIBLE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String savedStateMode = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "HomeTimeSelected", MONTH);
        setMyAppsDataToFirebase(savedStateMode);
        switch (savedStateMode) {
            case DAY:
                btnDay.setBackgroundResource(R.drawable.button_selected);
                break;
            case WEEK:
                btnWeek.setBackgroundResource(R.drawable.button_selected);
                break;
            case MONTH:
                btnMonth.setBackgroundResource(R.drawable.button_selected);
                break;
        }


        btnDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDay.setBackgroundResource(R.drawable.button_selected);
                btnDay.setActivated(true);
                btnWeek.setBackgroundResource(R.drawable.button_day_week_month);
                btnWeek.setActivated(false);
                btnMonth.setBackgroundResource(R.drawable.button_day_week_month);
                btnMonth.setActivated(false);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                prefs.edit().putString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "HomeTimeSelected", DAY).commit();
                setMyAppsDataToFirebase(DAY);

                refreshFragment();
            }
        });

        btnWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDay.setBackgroundResource(R.drawable.button_day_week_month);
                btnDay.setActivated(false);
                btnWeek.setBackgroundResource(R.drawable.button_selected);
                btnWeek.setActivated(true);
                btnMonth.setBackgroundResource(R.drawable.button_day_week_month);
                btnMonth.setActivated(false);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                prefs.edit().putString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "HomeTimeSelected", WEEK).commit();
                setMyAppsDataToFirebase(WEEK);

                refreshFragment();
            }
        });

        btnMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDay.setBackgroundResource(R.drawable.button_day_week_month);
                btnDay.setActivated(false);
                btnWeek.setBackgroundResource(R.drawable.button_day_week_month);
                btnWeek.setActivated(false);
                btnMonth.setBackgroundResource(R.drawable.button_selected);
                btnMonth.setActivated(true);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                prefs.edit().putString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "HomeTimeSelected", MONTH).commit();
                setMyAppsDataToFirebase(MONTH);
                /*startActivity(new Intent(getActivity(), HomeActivity.class));*/

                refreshFragment();
            }
        });



        /*Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.DAY_OF_WEEK, 1);
        cal.setFirstDayOfWeek(Calendar.MONDAY);*/

        /*if (btnDay.isActivated()) {
            Toast.makeText(getContext(), "DailyViewActive", Toast.LENGTH_SHORT).show();
        } else if (btnWeek.isActivated()) {
            Toast.makeText(getContext(), "WeekViewActive", Toast.LENGTH_SHORT).show();
        } else if (btnMonth.isActivated()) {
            Toast.makeText(getContext(), "MonthViewActive", Toast.LENGTH_SHORT).show();
        }*/

        appItemList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recycle_app_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        myAdapter = new RecyclerAdapter(appItemList);

        setAdapter();
        setAppInfo();
        saveTotalTimeToFirebase();

        return view;
    }

    public void refreshFragment() {
        FragmentTransaction ft = ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, new HomeFragment());
                ft.commit();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Date atStartOfDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setMyAppsDataToFirebase(String mode) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        switch (mode) {
            case DAY:
                cal.setTime(atStartOfDay(Calendar.getInstance().getTime()));
                break;

            case WEEK:
                cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
                cal.clear(Calendar.MINUTE);
                cal.clear(Calendar.SECOND);
                cal.clear(Calendar.MILLISECOND);

                cal.setFirstDayOfWeek(Calendar.MONDAY);

                // get start of this week in milliseconds
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

                /*Toast.makeText(getContext(), cal.getTime().toString(), Toast.LENGTH_SHORT).show();
                Toast.makeText(getContext(), convertTimeToString(System.currentTimeMillis() - cal.getTimeInMillis()), Toast.LENGTH_SHORT).show();*/

                break;

            case MONTH:
                cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
                cal.clear(Calendar.MINUTE);
                cal.clear(Calendar.SECOND);
                cal.clear(Calendar.MILLISECOND);

                cal.set(Calendar.DAY_OF_MONTH, 1);

                break;
        }

        getAppData(getContext(), "com.whatsapp", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "edu.salleurl.esyllabus", cal.getTimeInMillis(), System.currentTimeMillis());
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
        getAppData(getContext(), "com.facebook.katana", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.facebook.orca", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.reddit.frontpage", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.bereal.ft", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.vanced.android.youtube", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.fmwhatsapp", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.yowhatsapp", cal.getTimeInMillis(), System.currentTimeMillis());
        getAppData(getContext(), "com.tinder", cal.getTimeInMillis(), System.currentTimeMillis());
    }

    public void setDailyApps() {

    }

    public void getAppData(Context context, String packageName, long startSeason, long actualTime) {

        mUsageStatsManager = (UsageStatsManager)getContext().getSystemService(context.USAGE_STATS_SERVICE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String savedStateMode = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "HomeTimeSelected", MONTH);
        final List<UsageStats> stats;
        switch (savedStateMode) {
            case DAY:
                stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startSeason, actualTime);
                break;
            case WEEK:
                stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY, startSeason, actualTime);
                break;
            case MONTH:
                stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, startSeason, actualTime);
                break;
            default:
                return;
        }

        /*Log.e("HAHAHAHAHAHAHHAHAHHAHAHHA", "time: " + convertTimeToString(actualTime-startSeason));*/

        ArrayMap<String, UsageStats> map = new ArrayMap<>();
        final int statCount = stats.size();
        for (int i = 0; i < statCount; i++) {

            final android.app.usage.UsageStats pkgStats = stats.get(i);

            if (pkgStats.getPackageName().equals(packageName)) {
                // load application labels for each application

                String appName = getAppName(packageName);

                if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                    switch (savedStateMode) {
                        case DAY:
                            FirebaseDatabase.getInstance(FIREBASE_LINK)
                                    .getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("Apps")
                                    .child(packageName.replace(".", "-"))
                                    .child("timeDaily")
                                    .setValue(convertMillisToHourMinute(pkgStats.getTotalTimeInForeground()));
                            break;
                        case WEEK:
                            FirebaseDatabase.getInstance(FIREBASE_LINK)
                                    .getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("Apps")
                                    .child(packageName.replace(".", "-"))
                                    .child("timeWeekly")
                                    .setValue(convertMillisToHourMinute(pkgStats.getTotalTimeInForeground()));
                            break;
                        case MONTH:
                            FirebaseDatabase.getInstance(FIREBASE_LINK)
                                    .getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("Apps")
                                    .child(packageName.replace(".", "-"))
                                    .child("time")
                                    .setValue(convertMillisToHourMinute(pkgStats.getTotalTimeInForeground()));
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
        }
    }

    void saveTotalTimeToFirebase () {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                    .getReference("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("Apps");

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        final int[] totalTime = {0};
                        for (DataSnapshot dataSnapshot:snapshot.getChildren()) {
                            String time;
                            if (dataSnapshot.hasChild("time")) {
                                time = dataSnapshot.child("time").getValue().toString();
                            } else {
                                time = "00h 00m";
                            }
                            totalTime[0] += convertHourMinuteToInt(time);
                        }
                        // boolean if player is in the group or not to save its info
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                        Boolean yourLocked = prefs.getBoolean(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupStatus", true);
                        String prefsGroupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");
                        if (!yourLocked) {
                            if (!prefsGroupName.equals("null")) {

                                FirebaseDatabase.getInstance(FIREBASE_LINK)
                                        .getReference("Groups")
                                        .child(prefsGroupName)
                                        .child("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    if (snapshot.child("totalTaskMinutes").getValue() != null) {
                                                        String totalTaskMinutes = snapshot.child("totalTaskMinutes").getValue().toString();
                                                        totalTime[0] -= convertHourMinuteToInt(totalTaskMinutes);
                                                    }
                                                    String timeString = convertMillisToHourMinute(totalTime[0] *1000L);
                                                    FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                            .getReference("Groups")
                                                            .child(prefsGroupName)
                                                            .child("Users")
                                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                            .child("totalMinutes")
                                                            .setValue(timeString);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

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
                        String time = "00h 00m";
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                        String savedStateMode = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "HomeTimeSelected", MONTH);
                        switch (savedStateMode) {
                            case DAY:
                                if (dataSnapshot.hasChild("timeDaily")) {
                                    time = dataSnapshot.child("timeDaily").getValue().toString();
                                }
                                break;
                            case WEEK:
                                if (dataSnapshot.hasChild("timeWeekly")) {
                                    time = dataSnapshot.child("timeWeekly").getValue().toString();
                                }
                                break;
                            case MONTH:
                                if (dataSnapshot.hasChild("time")) {
                                    time = dataSnapshot.child("time").getValue().toString();
                                }
                                break;
                        }

                        Drawable drawableIcon = getActivity().getResources().getDrawable(R.drawable.no_icon);
                        if (name.equals("YouTube Vanced")) {
                            drawableIcon = getActivity().getResources().getDrawable(R.drawable.ic_youtube_svgrepo_com);
                        }

                        if (!time.equals("00h 00m")) {
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
                        public int compare(AppItem appItem1, AppItem appItem2) {
                            // ## Ascending order
                            /*return obj1.getAppName().compareToIgnoreCase(obj2.getAppName());*/
                            //return obj2.getTime().compareToIgnoreCase(obj1.getTime());
                            int time1 = convertHourMinuteToInt(appItem1.getAppTimeUsed());
                            int time2 = convertHourMinuteToInt(appItem2.getAppTimeUsed());
                            return Integer.compare(time2, time1); // To compare integer values

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

    public int convertTimeToSeconds(String timeString) {
        if (timeString.contains("d")) {
            String[] aux = timeString.split("d ", 2);
            String days = aux[0];
            String[] aux2 = aux[1].split("h ", 2);
            String hours = aux2[0];
            String[] aux3 = aux2[1].split("m ", 2);
            String mins = aux3[0];
            String[] aux4 = aux3[1].split("s", 2);
            String sec = aux4[0];
            return Integer.parseInt(days)*24*60*60 + Integer.parseInt(hours)*60*60 + Integer.parseInt(mins)*60 + Integer.parseInt(sec);
        } else if (timeString.contains("h")) {
            String[] aux = timeString.split("h ", 2);
            String hours = aux[0];
            String[] aux2 = aux[1].split("m ", 2);
            String mins = aux2[0];
            String[] aux3 = aux2[1].split("s", 2);
            String sec = aux3[0];
            return Integer.parseInt(hours)*60*60 + Integer.parseInt(mins)*60 + Integer.parseInt(sec);
        } else if (timeString.contains("m")) {
            String[] aux2 = timeString.split("m ", 2);
            String mins = aux2[0];
            String[] aux3 = aux2[1].split("s", 2);
            String sec = aux3[0];
            return Integer.parseInt(mins)*60 + Integer.parseInt(sec);
        } else {
            String removeSpace = timeString.replace(" ", "");
            String sec = removeSpace.replace("s", "");
            return Integer.parseInt(sec);
        }

    }

    private String convertTimeToString(long lastTimeUsed) {
        String format;
        long aDay = 86400000;
        long anHour = 3600000;
        long aMinute = 60000;
        if (lastTimeUsed >= aDay) {
            format = "%02dd %02dh %02dm %02ds";
            return String.format(
                    format,
                    TimeUnit.MILLISECONDS.toDays(lastTimeUsed),
                    TimeUnit.MILLISECONDS.toHours(lastTimeUsed) - TimeUnit.DAYS.toHours(
                            TimeUnit.MILLISECONDS.toDays(lastTimeUsed)
                    ),
                    TimeUnit.MILLISECONDS.toMinutes(lastTimeUsed) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(lastTimeUsed)
                    ),
                    TimeUnit.MILLISECONDS.toSeconds(lastTimeUsed) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(lastTimeUsed)
                    )
            );
        } else if (lastTimeUsed >= anHour) {
            format = "%02dh %02dm %02ds";
            return String.format(
                    format,
                    TimeUnit.MILLISECONDS.toHours(lastTimeUsed),
                    TimeUnit.MILLISECONDS.toMinutes(lastTimeUsed) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(lastTimeUsed)
                    ),
                    TimeUnit.MILLISECONDS.toSeconds(lastTimeUsed) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(lastTimeUsed)
                    )
            );
        } else if (lastTimeUsed >= aMinute) {
            format = "%02dm %02ds";
            return String.format(
                    format,
                    TimeUnit.MILLISECONDS.toMinutes(lastTimeUsed),
                    TimeUnit.MILLISECONDS.toSeconds(lastTimeUsed) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(lastTimeUsed)
                    )
            );
        } else {
            format = "%02ds";
            return String.format(
                    format,
                    TimeUnit.MILLISECONDS.toSeconds(lastTimeUsed)
            );
        }

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

}
