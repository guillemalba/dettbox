package com.glasswork.dettbox.ui.ranking;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.glasswork.dettbox.HomeActivity;
import com.glasswork.dettbox.R;
import com.glasswork.dettbox.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NoGroupFragment extends Fragment {

    private static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";
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
                        .getReference("Group");

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
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
                                        .getReference("Group")
                                        .child(groupName)
                                        .child("password")
                                        .setValue(groupPassword);

                                FirebaseDatabase.getInstance(FIREBASE_LINK)
                                        .getReference("Group")
                                        .child(groupName)
                                        .child("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("name")
                                        .setValue("Guille");    // TODO: change this value

                                FirebaseDatabase.getInstance(FIREBASE_LINK)
                                        .getReference("Group")
                                        .child(groupName)
                                        .child("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("totalMinutes")
                                        .setValue("300");       // TODO: Change this value


                                sendUserToNextFragment();
                                Toast.makeText(getContext(), "Group created successful, You are the Admin!", Toast.LENGTH_SHORT).show();
                            }
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

            }
        });



    }

    private void sendUserToNextFragment() {
        Fragment childFragment = new RankingFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, childFragment).commit();
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

        String groupName = etGroupName.getText().toString();
        String groupPassword = etGroupName.getText().toString();

        reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Group");
    }
}