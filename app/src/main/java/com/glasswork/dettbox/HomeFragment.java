package com.glasswork.dettbox;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.pm.PackageInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glasswork.dettbox.model.AppItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment {

    public static final String FIREBASE_DETTBOX_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";
    public static final String FIREBASE_TEST_LINK = "https://saveappusagetest-default-rtdb.firebaseio.com/";
    private ArrayList<AppItem> appItemList;
    private RecyclerView recyclerView;
    private RecyclerAdapter myAdapter;
    DatabaseReference dbReference;

    private String data;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app/";

    SharedPreferences sharedPreferences;

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

        setAdapter();
        setAppInfo();

        return view;
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
                .getReference("Apps");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        String time = dataSnapshot.child("time").getValue().toString();
                        Drawable drawableIcon = null;

                        List<PackageInfo> packList = getActivity().getPackageManager().getInstalledPackages(0);
                        for (int i=0; i < packList.size(); i++)
                        {
                            PackageInfo packInfo = packList.get(i);
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

        /*appItemList.add(new AppItem("WhatsApp", "2 horas"));
        appItemList.add(new AppItem("Deezer", "3 horas"));
        appItemList.add(new AppItem("Spotify", "3 horas"));
        appItemList.add(new AppItem("Instagram", "3 horas"));
        appItemList.add(new AppItem("Youtube", "3 horas"));
        appItemList.add(new AppItem("Telegram", "3 horas"));
        appItemList.add(new AppItem("Vet", "3 horas"));
        appItemList.add(new AppItem("Discord", "3 horas"));
        appItemList.add(new AppItem("WhatsApp", "2 horas"));
        appItemList.add(new AppItem("Deezer", "3 horas"));
        appItemList.add(new AppItem("Spotify", "3 horas"));
        appItemList.add(new AppItem("Instagram", "3 horas"));*/




    }

    /*public void storeApp(AppItem appItem, String btnName) {
        sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, 0);
        SharedPreferences.Editor save = sharedPreferences.edit();
        Toast.makeText(HomeActivity.this, btnName + " --> " + color , Toast.LENGTH_SHORT).show();
        Gson gson = new Gson();
        save.putInt(btnName, color);
        save.apply();
    }*/

    /*public AppItem getApp (AppItem btnName) {
        sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, 0);
        AppItem selectedColor = sharedPreferences.getInt(btnName, Color.WHITE);
        return selectedColor;
    }*/

}
