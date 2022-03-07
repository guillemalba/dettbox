package com.glasswork.dettbox.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.glasswork.dettbox.MainActivity;
import com.glasswork.dettbox.R;
import com.glasswork.dettbox.RegisterActivity;
import com.glasswork.dettbox.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private Button btnUpdate;
    private TextView textEmail;
    private EditText textName;
    private TextView textPassword;
    private EditText textBday;
    private TextView textGroup;
    private EditText TextName2;
    private EditText TextBday2;
    String _NAME, _BDAY;

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

        //Cogemos los campos
        textName = view.findViewById(R.id.textView5);
        textEmail = view.findViewById(R.id.textEmail);
        textPassword =  view.findViewById(R.id.textView9);
        textBday = view.findViewById(R.id.textView11);
        textGroup = view.findViewById(R.id.textView12);

        reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String pssw = Objects.requireNonNull(snapshot.child("password").getValue()).toString();
                    String bday = Objects.requireNonNull(snapshot.child("birth").getValue()).toString();
                    String groupName = snapshot.child("groupName").getValue().toString();

                    textName.setText(name);
                    textPassword.setText(pssw);
                    textBday.setText(bday);
                    textGroup.setText(groupName);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        textEmail.setText(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail());

        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mAuth.signOut();
                if(!isUsernameChanged() || !isBdayChanged()){

                    //Cogemos nuevos valores de los campos
                    TextName2 = view.findViewById(R.id.textView5);
                    TextBday2 = view.findViewById(R.id.textView11);
                    String newName = TextName2.getText().toString();
                    String email = textEmail.getText().toString();
                    String password = textPassword.getText().toString();
                    textPassword =  view.findViewById(R.id.textView9);
                    String newBday =  TextBday2.getText().toString();

                    //Metemos los nuevos valores en la bbdd
                    User user = new User(newName,email, password, newBday, "null");
                    FirebaseDatabase.getInstance("https://dettbox-default-rtdb.europe-west1.firebasedatabase.app")
                            .getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "User has been updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Data has not been updated!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }else{
                    Toast.makeText(getContext(),"There is nothing to update", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private  boolean isUsernameChanged(){
        return !_NAME.equals(textName.getText().toString());

    }

    private  boolean isBdayChanged(){
        return !_BDAY.equals(textBday.getText().toString());
    }
}
