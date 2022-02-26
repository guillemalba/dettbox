package com.glasswork.dettbox.ui.ranking;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.glasswork.dettbox.R;
import com.glasswork.dettbox.model.AppItem;
import com.glasswork.dettbox.model.User;
import com.glasswork.dettbox.ui.home.RecyclerAdapter;
import com.glasswork.dettbox.model.UserRanking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GrupListFragment extends Fragment {

    private static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";
    private DatabaseReference reference;
    private FirebaseAuth mAuth;

    private User user;

    private ArrayList<UserRanking> userRankingArrayList;
    private RecyclerView recyclerView;
    private RankingRecyclerAdapter myAdapter;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Button btnLeave;

    private TextView rankingName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_grup_list, container, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String groupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "Ranking");

        rankingName = getActivity().findViewById(R.id.ranking_name);
        rankingName.setText(groupName);
        readUser();
        mAuth = FirebaseAuth.getInstance();

        btnLeave = view.findViewById(R.id.btnLeave);
        btnLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveGroupDialog();
            }
        });

        userRankingArrayList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recycle_rank_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        myAdapter = new RankingRecyclerAdapter(userRankingArrayList);

        setAdapter();
        setUserInfo();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String groupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "Ranking");

        rankingName = getActivity().findViewById(R.id.ranking_name);
        rankingName.setText(groupName);
    }

    public void leaveGroupDialog() {
        dialogBuilder = new AlertDialog.Builder(getContext());
        final View leaveGroupView = getLayoutInflater().inflate(R.layout.popup_leave_group, null);


        dialogBuilder.setView(leaveGroupView);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Button btnYes = leaveGroupView.findViewById(R.id.btnYes);
        Button btnCancel = leaveGroupView.findViewById(R.id.btnCancel);

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                prefs.edit().putBoolean(mAuth.getCurrentUser().getUid(), true).commit();

                reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Groups").child(user.getGroupName()).child("Users").child(mAuth.getCurrentUser().getUid());
                reference.removeValue();

                FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("groupName")
                        .setValue("null");

                prefs.edit().putString(mAuth.getCurrentUser().getUid() + "groupName", "null").commit();

                sendUserToNextFragment();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private int userExist(ArrayList<UserRanking> list, String name) {
        int position = -2;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equals(name)) {
                return i;
            } else {
                position = -1;
            }
        }
        return position;
    }

    public void setUserInfo() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String prefsGroupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");
            FirebaseDatabase.getInstance(FIREBASE_LINK)
                    .getReference("Groups")
                    .child(prefsGroupName)
                    .child("Users")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot dataSnapshot:snapshot.getChildren()) {
                                    if(dataSnapshot.exists()) {
                                        String name = dataSnapshot.child("name").getValue().toString();
                                        String totalMinutes = "00h 00m 00s";
                                        if (dataSnapshot.child("totalMinutes").getValue() != null) {
                                            totalMinutes = dataSnapshot.child("totalMinutes").getValue().toString();
                                        }

                                        UserRanking userRanking = new UserRanking(dataSnapshot.getKey(), name, "1", totalMinutes);
                                        // if app already exist, update it
                                        int position = userExist(userRankingArrayList, name);
                                        if (position >= 0) {
                                            userRankingArrayList.get(position).setTime(totalMinutes);
                                        } else {
                                            userRankingArrayList.add(userRanking);
                                        }
                                    }
                                }
                                if (userRankingArrayList.size() >= 2) {
                                    Collections.sort(userRankingArrayList, new Comparator<UserRanking>(){
                                        public int compare(UserRanking obj1, UserRanking obj2) {
                                            // ## Ascending order
                                            /*return obj1.getAppName().compareToIgnoreCase(obj2.getAppName());*/
                                            //return obj2.getTime().compareToIgnoreCase(obj1.getTime());
                                            int time1 = convertTimeToSeconds(obj1.getTime());
                                            int time2 = convertTimeToSeconds(obj2.getTime());
                                            return Integer.compare(time1, time2); // To compare integer values

                                            // ## Descending order
                                            // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                                            // return Integer.valueOf(obj2.empId).compareTo(Integer.valueOf(obj1.empId)); // To compare integer values
                                        }
                                    });
                                }

                                for (int i = 0; i < userRankingArrayList.size(); i++) {
                                    userRankingArrayList.get(i).setPosition(String.valueOf((i+1)));
                                }
                                myAdapter.notifyDataSetChanged();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });



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

    public void setAdapter() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(myAdapter);
    }

    public void sendUserToNextFragment() {
        FragmentTransaction ft = ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new RankingFragment());
        ft.addToBackStack("GroupListFragment");
        ft.commit();
    }

    public void readUser()  {
        FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue().toString();
                            String surname = snapshot.child("surname").getValue().toString();
                            String pssw = snapshot.child("password").getValue().toString();
                            String bday = snapshot.child("birth").getValue().toString();
                            String groupName = snapshot.child("groupName").getValue().toString();
                            String email = snapshot.child("email").getValue().toString();
                            user = new User(name, surname, email, pssw, bday, groupName);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}