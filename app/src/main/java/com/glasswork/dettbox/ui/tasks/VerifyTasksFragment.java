package com.glasswork.dettbox.ui.tasks;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.glasswork.dettbox.Messages;
import com.glasswork.dettbox.R;
import com.glasswork.dettbox.model.ActiveTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class VerifyTasksFragment extends Fragment {

    private TextView tvTitle;
    private View vIcon;

    private static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";
    private ArrayList<ActiveTask> taskArrayList;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_verify_tasks, container, false);

        readGroupSize();

        taskArrayList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recycle_task_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        /*setAdapter();*/
        //setTaskInfo();

        setFirebaseAdapter();
        fetch();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putString("fragment_tasks", "null").commit();

        return view;
    }

    private void fetch() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String prefsGroupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");

        Query query = FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Groups")
                .child(prefsGroupName)
                .child("ActiveTasks");

        FirebaseRecyclerOptions<ActiveTask> options = new FirebaseRecyclerOptions.Builder<ActiveTask>()
                .setQuery(query, new SnapshotParser<ActiveTask>() {
                    @NonNull
                    @Override
                    public ActiveTask parseSnapshot(@NonNull DataSnapshot snapshot) {
                        String id = snapshot.child("id").getValue().toString();
                        String title = snapshot.child("title").getValue().toString();
                        String description = snapshot.child("description").getValue().toString();
                        String member1 = snapshot.child("member1").getValue().toString();
                        String member2 = snapshot.child("member2").getValue().toString();
                        String time = snapshot.child("time").getValue().toString();
                        String verifiedCount = snapshot.child("verifiedCount").getValue().toString();
                        return new ActiveTask(id, title, description, member1, member2, time, verifiedCount);
                    }
                })
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ActiveTask, MyViewHolder>(options) {
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_task_verified, parent, false);
                return new MyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i, @NonNull ActiveTask activeTask) {
                int count = prefs.getInt(prefsGroupName + "countUsers", 0);

                myViewHolder.setTvTaskId(activeTask.getId());
                myViewHolder.setTvTaskTitle(activeTask.getTitle());
                myViewHolder.setTvTaskDescription(activeTask.getDescription());
                myViewHolder.setTvTaskTime(activeTask.getTime());
                myViewHolder.setTvTaskReporter(activeTask.getMember1());
                myViewHolder.setTvTaskMember2("Participant: " + activeTask.getMember2());
                myViewHolder.setTvTaskVerifiedCount(activeTask.getVerifiedCount() + "/" + count);
                myViewHolder.setCardView();
                myViewHolder.setvCheck();

            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitleOnScreen();
    }

    public void setTitleOnScreen() {
        tvTitle = getActivity().findViewById(R.id.tvTitle);
        vIcon = getActivity().findViewById(R.id.icon_view);
        tvTitle.setText("Verify tasks");
        vIcon.setBackground(getContext().getDrawable(R.drawable.ic_task_verified2));
    }

    private int taskExist(ArrayList<ActiveTask> list, String id) {
        int position = -2;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(id)) {
                return i;
            } else {
                position = -1;
            }
        }
        return position;
    }

    public void setFirebaseAdapter() {

        // Reverses the order on retrieving data from firebase
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /*public void setTaskInfo() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String prefsGroupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");
        FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Groups")
                .child(prefsGroupName)
                .child("ActiveTasks")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            firebaseRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });



    }*/

    /*public long dateToMillis(String myDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(myDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long millis = date.getTime();
        return millis;
    }*/

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTaskId;
        private TextView tvTaskTime;
        private TextView tvTaskTitle;
        private TextView tvTaskReporter;
        private TextView tvTaskMember2;
        private TextView tvTaskDescription;
        private TextView tvTaskVerifiedCount;
        private View vCheck;
        private CardView cardView;
        private View viewListener;

        public MyViewHolder(@NonNull View view) {
            super(view);
            tvTaskId = view.findViewById(R.id.task_id);
            tvTaskTime = view.findViewById(R.id.task_time);
            tvTaskTitle = view.findViewById(R.id.task_title);
            tvTaskReporter = view.findViewById(R.id.task_reporter);
            tvTaskMember2 = view.findViewById(R.id.member2);
            tvTaskDescription = view.findViewById(R.id.task_description);
            tvTaskVerifiedCount = view.findViewById(R.id.verified_count);
            vCheck = view.findViewById(R.id.icon_check);
            cardView = view.findViewById(R.id.card_view);
            viewListener = view.findViewById(R.id.view_listener);

        }

        public void setTvTaskId(String string) {
            tvTaskId.setText(string);
        }

        public void setTvTaskTime(String string) {
            tvTaskTime.setText(string);
        }

        public void setTvTaskTitle(String string) {
            tvTaskTitle.setText(string);
        }

        public void setTvTaskReporter(String string) {
            tvTaskReporter.setText(string);
        }

        public void setTvTaskMember2(String string) {
            tvTaskMember2.setText(string);
        }

        public void setTvTaskDescription(String string) {
            tvTaskDescription.setText(string);
        }

        public void setTvTaskVerifiedCount(String string) {
            tvTaskVerifiedCount.setText(string);
        }

        public void setvCheck() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            Boolean active = prefs.getBoolean(FirebaseAuth.getInstance().getCurrentUser().getUid() + tvTaskId.getText().toString(), false);
            if (active) {
                vCheck.setBackgroundResource(R.drawable.ic_check_filled_green);
            } else {
                vCheck.setBackgroundResource(R.drawable.ic_check_filled);
            }

            vCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    String prefsGroupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + Messages.GROUP_NAME, "null");
                    FirebaseDatabase.getInstance(FIREBASE_LINK)
                            .getReference("Groups")
                            .child(prefsGroupName)
                            .child("ActiveTasks")
                            .child(tvTaskId.getText().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String id = snapshot.child("id").getValue().toString();
                                        String verifiedCount = snapshot.child("verifiedCount").getValue().toString();
                                        Boolean active = prefs.getBoolean(FirebaseAuth.getInstance().getCurrentUser().getUid() + id, false);
                                        int newValue = 0;
                                        if (active) {
                                            newValue = Integer.parseInt(verifiedCount) + 1;
                                        } else {
                                            newValue = Integer.parseInt(verifiedCount) - 1;
                                        }
                                        int numUsers = prefs.getInt(prefsGroupName + Messages.NUMBER_USERS, 0);
                                        if (newValue == numUsers) {
                                            FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                    .getReference("Groups")
                                                    .child(prefsGroupName)
                                                    .child("ActiveTasks")
                                                    .child(tvTaskId.getText().toString())
                                                    .removeValue();
                                            FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                    .getReference("Groups")
                                                    .child(prefsGroupName)
                                                    .child("Users")
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.exists()) {
                                                                for (DataSnapshot user : snapshot.getChildren()) {
                                                                    String userId = user.getKey();
                                                                    String username = user.child("name").getValue().toString();
                                                                    String participant = tvTaskMember2.getText().toString().replace("Participant: ", "");
                                                                    if(username.equals(participant) || username.equals(tvTaskReporter.getText().toString())) {
                                                                        int taskSeconds = 0;
                                                                        switch (tvTaskTime.getText().toString()) {
                                                                            case "1h": taskSeconds = 1*60*60; break;
                                                                            case "2h": taskSeconds = 2*60*60; break;
                                                                            case "3h": taskSeconds = 3*60*60; break;
                                                                            case "4h": taskSeconds = 4*60*60; break;
                                                                            case "5h": taskSeconds = 5*60*60; break;
                                                                        }

                                                                        if (user.hasChild("totalTaskMinutes")) {
                                                                            int totalTaskMinutes = convertTimeToSeconds(user.child("totalTaskMinutes").getValue().toString());
                                                                            taskSeconds += totalTaskMinutes;
                                                                        }
                                                                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                                                .getReference("Groups")
                                                                                .child(prefsGroupName)
                                                                                .child("Users")
                                                                                .child(userId)
                                                                                .child("totalTaskMinutes")
                                                                                .setValue(convertTimeToString(taskSeconds * 1000));

                                                                    }
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                        } else {
                                            FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                    .getReference("Groups")
                                                    .child(prefsGroupName)
                                                    .child("ActiveTasks")
                                                    .child(tvTaskId.getText().toString())
                                                    .child("verifiedCount")
                                                    .setValue(String.valueOf(newValue));
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                    Boolean active = prefs.getBoolean(FirebaseAuth.getInstance().getCurrentUser().getUid() + tvTaskId.getText().toString(), false);
                    if (active) {
                        vCheck.setBackgroundResource(R.drawable.ic_check_filled);
                        prefs.edit().putBoolean(FirebaseAuth.getInstance().getCurrentUser().getUid() + tvTaskId.getText().toString(), false).commit();
                    } else {
                        vCheck.setBackgroundResource(R.drawable.ic_check_filled_green);
                        prefs.edit().putBoolean(FirebaseAuth.getInstance().getCurrentUser().getUid() + tvTaskId.getText().toString(), true).commit();
                    }
                }
            });
        }

        public void setCardView() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            Boolean visible = prefs.getBoolean(FirebaseAuth.getInstance().getCurrentUser().getUid() + tvTaskId.getText().toString() + "card_visibility", false);

            if (visible) {
                tvTaskVerifiedCount.setVisibility(View.VISIBLE);
                tvTaskDescription.setVisibility(View.VISIBLE);
                tvTaskMember2.setVisibility(View.VISIBLE);

            } else {
                tvTaskDescription.setVisibility(View.GONE);
                tvTaskVerifiedCount.setVisibility(View.GONE);
                tvTaskMember2.setVisibility(View.GONE);

            }
            viewListener.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (tvTaskDescription.getVisibility() == View.VISIBLE) {
                        tvTaskDescription.setVisibility(View.GONE);
                        tvTaskVerifiedCount.setVisibility(View.GONE);
                        tvTaskMember2.setVisibility(View.GONE);
                        prefs.edit().putBoolean(FirebaseAuth.getInstance().getCurrentUser().getUid() + tvTaskId.getText().toString() + "card_visibility", false).commit();

                    } else if (tvTaskDescription.getVisibility() == View.GONE) {
                        tvTaskVerifiedCount.setVisibility(View.VISIBLE);
                        tvTaskDescription.setVisibility(View.VISIBLE);
                        tvTaskMember2.setVisibility(View.VISIBLE);
                        prefs.edit().putBoolean(FirebaseAuth.getInstance().getCurrentUser().getUid() + tvTaskId.getText().toString() + "card_visibility", true).commit();

                    }
                }
            });
        }
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

    public int convertTimeToSeconds(String timeString) {
        String[] aux = timeString.split("h ", 2);
        String hours = aux[0];
        String[] aux2 = aux[1].split("m ", 2);
        String mins = aux2[0];
        String[] aux3 = aux2[1].split("s", 2);
        String sec = aux3[0];
        return Integer.parseInt(hours)*60*60 + Integer.parseInt(mins)*60 + Integer.parseInt(sec);
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