package com.glasswork.dettbox.ui.ranking;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.glasswork.dettbox.R;
import com.glasswork.dettbox.model.User;
import com.glasswork.dettbox.ui.profile.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class NoGroupFragment extends Fragment {

    private static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseAuth mAuth;

    private User user;

    private Button btnCreateGroup;
    private Button btnJoinGroup;

    private EditText etGroupName;
    private EditText etGroupPassword;

    private FirebaseDatabase rootNode;
    private DatabaseReference reference;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_no_group, container, false);

        readUser();
        mAuth = FirebaseAuth.getInstance();

        btnCreateGroup = view.findViewById(R.id.btnCreateGroup);
        btnJoinGroup = view.findViewById(R.id.btnJoinGroup);

        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroupDialog();
            }
        });

        btnJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinGroupDialog();
            }
        });

        return view;
    }

    private void sendUserToNextFragment() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean(mAuth.getCurrentUser().getUid() + "groupStatus", false).commit();

        FragmentTransaction ft = ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new MainRankingFragment());
        ft.addToBackStack("GroupListFragment");
        ft.commit();
    }

    public void createGroupDialog() {
        dialogBuilder = new AlertDialog.Builder(getContext());
        final View createGroupView = getLayoutInflater().inflate(R.layout.popup_create_group, null);


        dialogBuilder.setView(createGroupView);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        etGroupName = createGroupView.findViewById(R.id.inputCreateGroupName);
        etGroupPassword = createGroupView.findViewById(R.id.inputCreateGroupPassword);
        Button btnCreate = createGroupView.findViewById(R.id.btnCreate);


        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String groupName = etGroupName.getText().toString();
                String groupPassword = etGroupPassword.getText().toString();

                reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Groups");

                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean exist = false;
                        for (DataSnapshot dataSnapshot:snapshot.getChildren()) {
                            if (groupName.equals(dataSnapshot.getKey())) {
                                exist = true;
                            }
                        }
                        if (exist) {
                            Toast.makeText(getContext(), "This group name already exist.", Toast.LENGTH_SHORT).show();
                        } else {
                            FirebaseDatabase.getInstance(FIREBASE_LINK)
                                    .getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String name = snapshot.child("name").getValue().toString();

                                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                .getReference("Groups")
                                                .child(groupName)
                                                .child("password")
                                                .setValue(groupPassword);

                                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                .getReference("Groups")
                                                .child(groupName)
                                                .child("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("name")
                                                .setValue(user.getName());    // TODO: change this value

                                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                .getReference("Groups")
                                                .child(groupName)
                                                .child("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("totalMinutes")
                                                .setValue("00h 00m");       // TODO: Change this value

                                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                .getReference("Groups")
                                                .child(groupName)
                                                .child("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("totalTaskMinutes")
                                                .setValue("00h 00m");       // TODO: Change this value

                                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                .getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("groupName")
                                                .setValue(groupName);

                                        dialog.dismiss();

                                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                                        prefs.edit().putString(mAuth.getCurrentUser().getUid() + "groupName", groupName).commit();

                                        // we set the default list of rewards and punishments from a csv file and store it to firebase
                                        setDefaultRewardsToFirebase();
                                        setDefaultPunishmentsToFirebase();

                                        sendUserToNextFragment();
                                        Toast.makeText(getContext(), "Group created successful, You are the Admin!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

            }
        });



    }

    public void joinGroupDialog() {
        dialogBuilder = new AlertDialog.Builder(getContext());
        final View joinGroupView = getLayoutInflater().inflate(R.layout.popup_join_group, null);

        dialogBuilder.setView(joinGroupView);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        etGroupName = joinGroupView.findViewById(R.id.inputJoinGroupName);
        etGroupPassword = joinGroupView.findViewById(R.id.inputJoinGroupPassword);

        Button btnJoin = joinGroupView.findViewById(R.id.btnJoin);

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String groupName = etGroupName.getText().toString();
                String groupPassword = etGroupPassword.getText().toString();

                reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Groups");

                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            boolean exist = false;
                            for (DataSnapshot dataSnapshot:snapshot.getChildren()) {
                                if (groupName.equals(dataSnapshot.getKey()) && groupPassword.equals(dataSnapshot.child("password").getValue().toString())) {
                                    exist = true;
                                }
                            }
                            if (exist) {
                                FirebaseDatabase.getInstance(FIREBASE_LINK)
                                        .getReference("Groups")
                                        .child(groupName)
                                        .child("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("name")
                                        .setValue(user.getName());    // TODO: change this value

                                FirebaseDatabase.getInstance(FIREBASE_LINK)
                                        .getReference("Groups")
                                        .child(groupName)
                                        .child("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("totalMinutes")
                                        .setValue("00h 00m");       // TODO: Change this value

                                FirebaseDatabase.getInstance(FIREBASE_LINK)
                                        .getReference("Groups")
                                        .child(groupName)
                                        .child("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("totalTaskMinutes")
                                        .setValue("00h 00m");       // TODO: Change this value

                                FirebaseDatabase.getInstance(FIREBASE_LINK)
                                        .getReference("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("groupName")
                                        .setValue(groupName);

                                dialog.dismiss();

                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                                prefs.edit().putString(mAuth.getCurrentUser().getUid() + "groupName", groupName).commit();

                                sendUserToNextFragment();

                                Toast.makeText(getContext(), "Joined successful!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Group name or password not matches.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "There are no Groups yet!", Toast.LENGTH_SHORT).show();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
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
                    user = new User(name,email, pssw, groupName);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setDefaultRewardsToFirebase() {
        List<String> listRewards = new ArrayList<>();

        InputStream is = getResources(). openRawResource(R.raw.default_rewards);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String reward = "";
        try {
            while ((reward = reader.readLine()) != null) {
                listRewards.add(reward);
            }
        } catch (IOException e) {
            Log.e("CSV", "Error reading data file on line " + reward);
        }

        for (int i = 0; i < listRewards.size(); i++) {
            String position = "" + i;
            if (i < 10) {
                position = "0" + i;
            }
            String groupName = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");

            FirebaseDatabase.getInstance(FIREBASE_LINK)
                    .getReference("Groups")
                    .child(groupName)
                    .child("Rewards")
                    .child("default-" + position)
                    .child("title")
                    .setValue(listRewards.get(i));
        }
    }

    private void setDefaultPunishmentsToFirebase() {
        List<String> listPunishments = new ArrayList<>();

        InputStream is = getResources(). openRawResource(R.raw.default_punishments);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String punishment = "";
        try {
            while ((punishment = reader.readLine()) != null) {
                listPunishments.add(punishment);
            }
        } catch (IOException e) {
            Log.e("CSV", "Error reading data file on line " + punishment);
        }

        for (int i = 0; i < listPunishments.size(); i++) {
            String position = "" + i;
            if (i < 10) {
                position = "0" + i;
            }
            String groupName = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");

            FirebaseDatabase.getInstance(FIREBASE_LINK)
                    .getReference("Groups")
                    .child(groupName)
                    .child("Punishments")
                    .child("default-" + position)
                    .child("title")
                    .setValue(listPunishments.get(i));
        }
    }

    public void refreshFragment() {
        FragmentTransaction ft = ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new MainRankingFragment());
        ft.commit();
    }
}