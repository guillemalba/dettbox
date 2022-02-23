package com.glasswork.dettbox.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.glasswork.dettbox.MainActivity;
import com.glasswork.dettbox.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    private static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";
    private DatabaseReference reference;

    private FirebaseAuth mAuth;
    private Button btnLogout;
    private TextView textEmail;
    private TextView textName;
    private TextView textSurname;
    private TextView textPassword;
    private TextView textBday;
    private TextView textGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        btnLogout = view.findViewById(R.id.btnlogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

         FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Group")
                .child(mAuth.getUid())
                .child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("name")
                .setValue("Guille");

        //Cogemos los campos
        textName = view.findViewById(R.id.textView5);
        textSurname = view.findViewById(R.id.textView7);
        textEmail = view.findViewById(R.id.textEmail);
        textPassword =  view.findViewById(R.id.textView9);
        textBday = view.findViewById(R.id.textView11);
        textGroup = view.findViewById(R.id.textView12);

        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue().toString();
                    String surname = snapshot.child("surname").getValue().toString();
                    String pssw = snapshot.child("password").getValue().toString();
                    String bday = snapshot.child("birth").getValue().toString();

                    textName.setText(name);
                    textSurname.setText(surname);
                    textPassword.setText(pssw);
                    textBday.setText(bday);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        textEmail.setText(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail());

        return view;
    }
}
