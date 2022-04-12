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

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    private EditText etTaskTitle;
    private EditText etTaskDescription;
    private Spinner spinnerMembers;
    private Spinner spinnerHours;
    private Button btnAddTask;
    private View vAddTask;
    private View vVerifyTask;

    private List<String> names;
    private List<String> hours;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_tasks, container, false);

        names = new ArrayList<>();
        hours = new ArrayList<>();
        hours.add("1h");
        hours.add("2h");
        hours.add("3h");
        hours.add("4h");
        hours.add("5h");

        // reads the user and stores it on shared preferences
        readUser();

        // reads de group size
        readGroupSize();

        vAddTask = view.findViewById(R.id.add_task);
        vAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTaskDialog();
            }
        });

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

        return view;
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

        btnAddTask = addTaskView.findViewById(R.id.btnAddTask);

        btnAddTask.setOnClickListener(new View.OnClickListener() {
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
                            String bday = snapshot.child("birth").getValue().toString();
                            String groupName = snapshot.child("groupName").getValue().toString();
                            String email = snapshot.child("email").getValue().toString();

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                            Gson gson = new Gson();
                            String stringUser = gson.toJson(new User(name,email, pssw, bday, groupName));
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