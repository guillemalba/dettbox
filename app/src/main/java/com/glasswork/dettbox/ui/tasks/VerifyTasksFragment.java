package com.glasswork.dettbox.ui.tasks;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.glasswork.dettbox.Messages;
import com.glasswork.dettbox.R;
import com.glasswork.dettbox.model.ActiveTask;
import com.glasswork.dettbox.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class VerifyTasksFragment extends Fragment {

    private TextView tvTitle;
    private View vIcon;

    private static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";
    private ArrayList<ActiveTask> taskArrayList;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    private Button btnAddTask;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText etTaskTitle;
    private EditText etTaskDescription;
    private Spinner spinnerMembers;
    private Spinner spinnerHours;
    private Button btnAdd;

    private List<String> names;
    private List<String> hours;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_verify_tasks, container, false);

        names = new ArrayList<>();
        hours = new ArrayList<>();
        hours.add("1h");
        hours.add("2h");
        hours.add("3h");
        hours.add("4h");
        hours.add("5h");

        btnAddTask = getActivity().findViewById(R.id.btnAddTask);
        btnAddTask.setVisibility(View.VISIBLE);
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTaskDialog();
            }
        });

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

    public void addTaskDialog() {
        dialogBuilder = new AlertDialog.Builder(getContext());
        final View addTaskView = getLayoutInflater().inflate(R.layout.popup_add_task, null);

        dialogBuilder.setView(addTaskView);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        etTaskTitle = addTaskView.findViewById(R.id.inputTaskTitle);
        etTaskDescription = addTaskView.findViewById(R.id.inputTaskDescription);
        spinnerMembers = addTaskView.findViewById(R.id.spinner_members);
        spinnerHours = addTaskView.findViewById(R.id.spinner_hours);

        btnAdd = addTaskView.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                String groupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");

                Gson gson = new Gson();
                String json = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid(), "");
                User user = gson.fromJson(json, User.class);

                String taskTitle = etTaskTitle.getText().toString();
                String taskDescription = etTaskDescription.getText().toString();
                String memberName2;
                int groupSize = prefs.getInt(groupName + "countUsers", 0);
                if (groupSize == 1) {
                    memberName2 = "Nobody";
                } else {
                    memberName2 = spinnerMembers.getSelectedItem().toString();
                }
                String hoursSelected = spinnerHours.getSelectedItem().toString();

                String actualDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now());
                String id = actualDate;

                ActiveTask newTask = new ActiveTask(id, taskTitle, taskDescription, user.getName(), memberName2, hoursSelected, "0");
                FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Groups")
                        .child(groupName)
                        .child("ActiveTasks")
                        .child(actualDate)
                        .setValue(newTask);
                Toast.makeText(getContext(), "Task successfully added!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String groupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");

        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Groups")
                .child(groupName)
                .child("Users");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    /*LinkedList<String> groupMembers = new LinkedList<>();*/
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    Gson gson = new Gson();
                    String json = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid(), "");
                    User user = gson.fromJson(json, User.class);
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()) {
                        String member = dataSnapshot.child("name").getValue().toString();
                        if (!user.getName().equals(member)) {
                            names.add(member);
                        }
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_checked, names);
                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

                        spinnerMembers.setAdapter(arrayAdapter);

                        ArrayAdapter<String> arrayHoursAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_checked, hours);
                        arrayHoursAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

                        spinnerHours.setAdapter(arrayHoursAdapter);
                    }

                            /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                            String fragment = prefs.getString("fragment_tasks", "null");
                            prefs.edit().putString(mAuth.getCurrentUser().getUid() + "groupName", "null").commit();
                            String groupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");*/
                } else {
                    Toast.makeText(getContext(), "ERROR: Im here on MenuTasksFragment", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        btnAddTask.setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
        btnAddTask.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitleOnScreen();
        btnAddTask.setVisibility(View.VISIBLE);
    }

    public void setTitleOnScreen() {
        tvTitle = getActivity().findViewById(R.id.tvTitle);
        vIcon = getActivity().findViewById(R.id.icon_view);
        tvTitle.setText(R.string.verify_tasks);
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
                                                                            int totalTaskMinutes = convertHourMinuteToInt(user.child("totalTaskMinutes").getValue().toString());
                                                                            taskSeconds += totalTaskMinutes;
                                                                        }
                                                                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                                                .getReference("Groups")
                                                                                .child(prefsGroupName)
                                                                                .child("Users")
                                                                                .child(userId)
                                                                                .child("totalTaskMinutes")
                                                                                .setValue(convertMillisToHourMinute(taskSeconds * 1000));

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