package com.glasswork.dettbox.ui.tasks;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.glasswork.dettbox.R;
import com.glasswork.dettbox.model.ActiveTask;
import com.glasswork.dettbox.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MenuTasksFragment extends Fragment {
    private static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";

    private View vAddTask;
    private View vVerifyTask;
    private View vManageRewards;
    private View vManagePunishments;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_tasks, container, false);

        // reads the user and stores it on shared preferences
        readUser();

        // reads de group size
        readGroupSize();

        vVerifyTask = view.findViewById(R.id.verify_task);
        vVerifyTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                prefs.edit().putString("fragment_tasks", "verify_task").commit();

                Fragment fragment = new MainTasksFragment();
                //replacing the fragment
                if (fragment != null) {
                    FragmentTransaction ft = ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.fragment_container, fragment);
                    ft.addToBackStack("verify_task");
                    ft.commit();
                }
            }
        });

        vManageRewards = view.findViewById(R.id.manage_reward);
        vManageRewards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                prefs.edit().putString("fragment_tasks", "manage_reward").commit();

                Fragment fragment = new MainTasksFragment();
                //replacing the fragment
                if (fragment != null) {
                    FragmentTransaction ft = ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.fragment_container, fragment);
                    ft.addToBackStack("manage_reward");
                    ft.commit();
                }
            }
        });

        vManagePunishments = view.findViewById(R.id.manage_punishment);
        vManagePunishments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                prefs.edit().putString("fragment_tasks", "manage_punishment").commit();

                Fragment fragment = new MainTasksFragment();
                //replacing the fragment
                if (fragment != null) {
                    FragmentTransaction ft = ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.fragment_container, fragment);
                    ft.addToBackStack("manage_punishment");
                    ft.commit();
                }
            }
        });

        return view;
    }

    public void readUser() {
        FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue().toString();
                            String pssw = snapshot.child("password").getValue().toString();
                            String groupName = snapshot.child("groupName").getValue().toString();
                            String email = snapshot.child("email").getValue().toString();

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                            Gson gson = new Gson();
                            String stringUser = gson.toJson(new User(name,email, pssw, groupName));
                            prefs.edit().putString(FirebaseAuth.getInstance().getCurrentUser().getUid(), stringUser).commit();

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public void readGroupSize() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String prefsGroupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");
        FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Groups")
                .child(prefsGroupName)
                .child("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int counter = 0;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            counter++;
                        }
                        prefs.edit().putInt(prefsGroupName + "countUsers", counter).commit();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}