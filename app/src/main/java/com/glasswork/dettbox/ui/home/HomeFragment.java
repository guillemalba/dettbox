package com.glasswork.dettbox.ui.home;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
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

import com.glasswork.dettbox.R;
import com.glasswork.dettbox.model.AppItem;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import bot.box.appusage.contract.PackageContracts;
import bot.box.appusage.handler.Monitor;
import bot.box.appusage.model.AppData;
import bot.box.appusage.utils.DurationRange;
import kotlin.jvm.internal.unsafe.MonitorKt;

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

        String savedStateMode = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "HomeTimeSelected", MONTH);
        switch (savedStateMode) {
            case DAY:
                btnDay.setBackgroundResource(R.drawable.button_selected);
                btnDay.setTextColor(Color.parseColor("#000000"));
                break;
            case WEEK:
                btnWeek.setBackgroundResource(R.drawable.button_selected);
                btnWeek.setTextColor(Color.parseColor("#000000"));
                break;
            case MONTH:
                btnMonth.setBackgroundResource(R.drawable.button_selected);
                btnMonth.setTextColor(Color.parseColor("#000000"));
                break;
        }

        btnDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDay.setBackgroundResource(R.drawable.button_selected);
                btnWeek.setBackgroundResource(R.drawable.button_day_week_month);
                btnMonth.setBackgroundResource(R.drawable.button_day_week_month);
                btnDay.setTextColor(Color.parseColor("#000000"));
                btnWeek.setTextColor(Color.parseColor("#FFFFFF"));
                btnMonth.setTextColor(Color.parseColor("#FFFFFF"));


                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "HomeTimeSelected", DAY).commit();
                refreshFragment();
            }
        });

        btnWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDay.setBackgroundResource(R.drawable.button_day_week_month);
                btnWeek.setBackgroundResource(R.drawable.button_selected);
                btnMonth.setBackgroundResource(R.drawable.button_day_week_month);
                btnDay.setTextColor(Color.parseColor("#FFFFFF"));
                btnWeek.setTextColor(Color.parseColor("#000000"));
                btnMonth.setTextColor(Color.parseColor("#FFFFFF"));


                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "HomeTimeSelected", WEEK).commit();
                refreshFragment();
            }
        });

        btnMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDay.setBackgroundResource(R.drawable.button_day_week_month);
                btnWeek.setBackgroundResource(R.drawable.button_day_week_month);
                btnMonth.setBackgroundResource(R.drawable.button_selected);
                btnDay.setTextColor(Color.parseColor("#FFFFFF"));
                btnWeek.setTextColor(Color.parseColor("#FFFFFF"));
                btnMonth.setTextColor(Color.parseColor("#000000"));

                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "HomeTimeSelected", MONTH).commit();
                refreshFragment();
            }
        });

        appItemList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recycle_app_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        myAdapter = new RecyclerAdapter(appItemList);

        setAdapter();
        setAppInfo();

        return view;
    }

    public void refreshFragment() {
        FragmentTransaction ft = ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new HomeFragment());
        ft.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        btnDay.setVisibility(View.GONE);
        btnWeek.setVisibility(View.GONE);
        btnMonth.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        btnDay.setVisibility(View.VISIBLE);
        btnWeek.setVisibility(View.VISIBLE);
        btnMonth.setVisibility(View.VISIBLE);
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
                            drawableIcon = getActivity().getResources().getDrawable(R.drawable.ic_youtube);
                        }
                        if (name.equals("eSyllabus")) {
                            drawableIcon = getActivity().getResources().getDrawable(R.drawable.ic_ls_bcn);
                        }

                        if (!time.equals("00h 00m")) {
                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                            String json = prefs.getString(name, null);
                            if (json != null) {
                                PackageInfo packInfo = gson.fromJson(json, PackageInfo.class);
                                String appName = packInfo.applicationInfo.loadLabel(getActivity().getPackageManager()).toString();
                                Log.e("name--", name + "--" + appName);
                                if (appName.equals("com.instagram.app.InstagramAppShell")) {
                                    drawableIcon = getActivity().getResources().getDrawable(R.drawable.ic_instagram);
                                }
                                if (appName.equals(name)) {
                                    drawableIcon = packInfo.applicationInfo.loadIcon(getActivity().getPackageManager());
                                    /*if (name.equals("Dettbox")) {
                                        drawableIcon = getActivity().getResources().getDrawable(R.drawable.ic_dettbox2);
                                    }*/
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

    public int convertHourMinuteToInt(String timeString) {
        String[] aux = timeString.split("h ", 2);
        String hours = aux[0];
        String[] aux2 = aux[1].split("m", 2);
        String mins = aux2[0];
        return Integer.parseInt(hours)*60*60 + Integer.parseInt(mins)*60;
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