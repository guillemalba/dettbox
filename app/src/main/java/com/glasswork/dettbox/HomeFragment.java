package com.glasswork.dettbox;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    public static final String FIREBASE_DETTBOX_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";
    public static final String FIREBASE_TEST_LINK = "https://saveappusagetest-default-rtdb.firebaseio.com/";
    private ArrayList<AppItem> appItemList;
    private RecyclerView recyclerView;
    private RecyclerAdapter myAdapter;
    DatabaseReference dbReference;

    private String data;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";

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
                        AppItem appItem = new AppItem(name, time);
                        int position = appExist(appItemList, name);
                        if (position >= 0) {
                            appItemList.get(position).setAppTimeUsed(time);
                        } else {
                            appItemList.add(appItem);
                        }
                    }
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

    public void storeButton(int color, String btnName) {
        sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, 0);
        SharedPreferences.Editor save = sharedPreferences.edit();
        /*Toast.makeText(HomeActivity.this, btnName + " --> " + color , Toast.LENGTH_SHORT).show();*/
        save.putInt(btnName, color);
        save.apply();
    }

    public int getColor (String btnName) {
        sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, 0);
        int selectedColor = sharedPreferences.getInt(btnName, Color.WHITE);
        return selectedColor;
    }

}
