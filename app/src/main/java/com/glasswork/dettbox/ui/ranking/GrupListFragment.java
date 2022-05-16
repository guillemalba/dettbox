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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.glasswork.dettbox.R;
import com.glasswork.dettbox.model.User;
import com.glasswork.dettbox.model.UserRanking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

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

    private TextView tvCountDown;
    CountDownTimer mCountDownTimer;

    long mInitialTime;
    long timeRemaining;

    private Button btnLastSeason;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_grup_list, container, false);

        tvCountDown = view.findViewById(R.id.tvCountDown);
        btnLastSeason = view.findViewById(R.id.btn_last_season);
        // sets the timer of the season
        setCountDownSeason();

        // sets the group name as the title of ranking group
        setGroupNameOnScreen();

        readUser();
        mAuth = FirebaseAuth.getInstance();

        btnLastSeason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, new FinalResultsFragment());
                ft.addToBackStack("FinalResultsFragment");
                ft.commit();
            }
        });

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
        myAdapter = new RankingRecyclerAdapter(userRankingArrayList, getContext());

        setAdapter();
        setUserInfo();
        saveTotalTimeToFirebase();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String groupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "Ranking");

        rankingName = getActivity().findViewById(R.id.ranking_name);
        /*rankingName.setText(groupName);*/
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
                        final int[] totalTime = {0};
                        for (DataSnapshot dataSnapshot:snapshot.getChildren()) {
                            String time = dataSnapshot.child("time").getValue().toString();
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

                                                    // save hours as int to change led color on the box
                                                    String hours[] = timeString.split("h");
                                                    FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                            .getReference("Groups")
                                                            .child(prefsGroupName)
                                                            .child("Users")
                                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                            .child("totalHours")
                                                            .setValue(hours[0]);
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

    public void setGroupNameOnScreen() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String groupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "Ranking");

        rankingName = getActivity().findViewById(R.id.ranking_name);
        rankingName.setText(groupName);
    }

    public void setCountDownSeason() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        cal.set(Calendar.DAY_OF_MONTH, 1);

        mInitialTime = DateUtils.DAY_IN_MILLIS * Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
        long timeBetweenStartMonthAndCurrent = System.currentTimeMillis() - cal.getTimeInMillis();
        timeRemaining = mInitialTime - timeBetweenStartMonthAndCurrent;
        long cambioDeHoraEspaña = 3600000;
        //timeRemaining = 10000;
        mCountDownTimer = new CountDownTimer(timeRemaining, 1000) {
            StringBuilder time = new StringBuilder();
            @Override
            public void onFinish() {
                FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Groups")
                        .child(user.getGroupName())
                        .child("FinalResults")
                        .child("Winner")
                        .child("id")
                        .setValue(userRankingArrayList.get(0).getId());

                FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Groups")
                        .child(user.getGroupName())
                        .child("FinalResults")
                        .child("Winner")
                        .child("name")
                        .setValue(userRankingArrayList.get(0).getName());

                FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Groups")
                        .child(user.getGroupName())
                        .child("FinalResults")
                        .child("Winner")
                        .child("time")
                        .setValue(userRankingArrayList.get(0).getTime());

                FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Groups")
                        .child(user.getGroupName())
                        .child("FinalResults")
                        .child("Winner")
                        .child("position")
                        .setValue(userRankingArrayList.get(0).getPosition());


                FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Groups")
                        .child(user.getGroupName())
                        .child("FinalResults")
                        .child("Loser")
                        .child("id")
                        .setValue(userRankingArrayList.get(userRankingArrayList.size()-1).getId());

                FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Groups")
                        .child(user.getGroupName())
                        .child("FinalResults")
                        .child("Loser")
                        .child("name")
                        .setValue(userRankingArrayList.get(userRankingArrayList.size()-1).getName());

                FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Groups")
                        .child(user.getGroupName())
                        .child("FinalResults")
                        .child("Loser")
                        .child("time")
                        .setValue(userRankingArrayList.get(userRankingArrayList.size()-1).getTime());

                FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Groups")
                        .child(user.getGroupName())
                        .child("FinalResults")
                        .child("Loser")
                        .child("position")
                        .setValue(userRankingArrayList.get(userRankingArrayList.size()-1).getPosition());

                tvCountDown.setText("Season Ended");
                //mTextView.setText("Times Up!");
                FragmentTransaction ft = ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, new FinalResultsFragment());
                ft.addToBackStack("FinalResultsFragment");
                ft.commit();
                // TODO: here finishes counter
            }

            @Override
            public void onTick(long millisUntilFinished) {
                time.setLength(0);
                time.append(convertTimeToString(millisUntilFinished));
                tvCountDown.setText(time.toString());
            }
        }.start();
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
                prefs.edit().putBoolean(mAuth.getCurrentUser().getUid() + "groupStatus", true).commit();

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
                                        String totalMinutes = "00h 00m";
                                        String totalTaskMinutes = "00h 00m";
                                        if (dataSnapshot.child("totalMinutes").getValue() != null) {
                                            totalMinutes = dataSnapshot.child("totalMinutes").getValue().toString();
                                        }

                                        UserRanking userRanking = new UserRanking(dataSnapshot.getKey(), name, "1", totalMinutes, totalTaskMinutes);
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
                                            int time1 = convertHourMinuteToInt(obj1.getTime());
                                            int time2 = convertHourMinuteToInt(obj2.getTime());
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
            System.out.println("KO");
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

    public void setAdapter() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(myAdapter);
    }

    public void sendUserToNextFragment() {
        FragmentTransaction ft = ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new MainRankingFragment());
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
                            String pssw = snapshot.child("password").getValue().toString();
                            String groupName = snapshot.child("groupName").getValue().toString();
                            String email = snapshot.child("email").getValue().toString();
                            user = new User(name, email, pssw, groupName);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}