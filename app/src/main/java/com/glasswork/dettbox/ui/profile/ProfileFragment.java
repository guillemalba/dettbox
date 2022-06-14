package com.glasswork.dettbox.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glasswork.dettbox.MainActivity;
import com.glasswork.dettbox.Messages;
import com.glasswork.dettbox.R;
import com.glasswork.dettbox.RegisterActivity;
import com.glasswork.dettbox.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";
    public static final String[] colors = {"blue","red","green","yellow","magenta","cyan","orange"};
    private DatabaseReference reference;

    private FirebaseAuth mAuth;
    private Button btnLogout;
    private Button btnUpdate;
    private TextView textEmail;
    private EditText textName;
    private TextView textPassword;
    private TextView textGroup;
    private EditText TextName2;
    private Spinner spinnerColors;


    private CircularImageView profilePic;
    private Button btnEditPic;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    RecyclerView.LayoutManager layoutManager;
    private StorageReference storageReference;

    private String selectedColor;

    String _NAME;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();

        spinnerColors = view.findViewById(R.id.spinner_colors);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_checked, colors);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spinnerColors.setAdapter(adapter);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        int selected;
        selectedColor = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "backgroundColor", "blue");

        switch (selectedColor) {
            case "blue":
                selected = 0;
                break;
            case "red":
                selected = 1;
                break;
            case "green":
                selected = 2;
                break;
            case "yellow":
                selected = 3;
                break;
            case "magenta":
                selected = 4;
                break;
            case "cyan":
                selected = 5;
                break;
            case "orange":
                selected = 6;
                break;

            default:
                selected = 1;
                break;

        }


        spinnerColors.setSelection(selected);
        spinnerColors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String selectedLang =  adapterView.getItemAtPosition(i).toString();
                FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("backgroundColor")
                        .setValue(selectedLang);

                prefs.edit().putString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "backgroundColor", selectedLang).commit();


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



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
        textGroup = view.findViewById(R.id.textView12);
        profilePic = view.findViewById(R.id.profile_foto);
        btnEditPic = view.findViewById(R.id.edit_pic_button);


        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFotoPicker();
            }
        });

        btnEditPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFotoPicker();
            }
        });

        reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String pssw = Objects.requireNonNull(snapshot.child("password").getValue()).toString();
                    String groupName = Objects.requireNonNull(snapshot.child("groupName").getValue()).toString();
                    if (snapshot.hasChild("picture")) {
                        String picturePath = snapshot.child("picture").getValue().toString();
                        Picasso.get()
                                .load(picturePath)
                                .fit()
                                .centerCrop()
                                .into(profilePic);
                    } else {
                        profilePic.setImageResource(R.drawable.ic_profile_picture_register);
                    }

                    textName.setText(name);
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

                //Cogemos nuevos valores de los campos
                TextName2 = view.findViewById(R.id.textView5);
                _NAME = TextName2.getText().toString();

                if(!isUsernameChanged()) {
                    //Metemos los nuevos valores en la bbdd
                    String email = textEmail.getText().toString();
                    String password = textPassword.getText().toString();
                    String group =  textGroup.getText().toString();
                    /*textPassword =  view.findViewById(R.id.textView9);*/

                    User user = new User(_NAME, email, password, group);
                    FirebaseDatabase.getInstance(FIREBASE_LINK)
                            .getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Username has been updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Username has not been updated!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    String prefsGroupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");
                    FirebaseDatabase.getInstance(FIREBASE_LINK)
                            .getReference("Groups")
                            .child(prefsGroupName)
                            .child("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("name")
                            .setValue(_NAME);
                }else{
                    Toast.makeText(getContext(),"There is nothing to update", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    public void showFotoPicker() {

        dialogBuilder = new AlertDialog.Builder(getContext());
        final View dialogView = getLayoutInflater().inflate(R.layout.popup_edit_profile_pic, null);

        storageReference = FirebaseStorage.getInstance().getReference();
        List<String> listImages = new ArrayList<>();
        listImages.add(Messages.CARA1_CONTENT.toString());
        listImages.add(Messages.CARA1_MIG.toString());
        listImages.add(Messages.CARA1_TRIST.toString());
        listImages.add(Messages.CARA2_CONTENT.toString());
        listImages.add(Messages.CARA2_MIG.toString());
        listImages.add(Messages.CARA2_TRIST.toString());

        dialogBuilder.setView(dialogView);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        recyclerView = dialogView.findViewById(R.id.recycle_profile_pics);
        layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), dialog, listImages);

        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setHasFixedSize(true);

    }

    @Override
    public void onResume() {
        super.onResume();
        reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String pssw = Objects.requireNonNull(snapshot.child("password").getValue()).toString();
                    String groupName = Objects.requireNonNull(snapshot.child("groupName").getValue()).toString();
                    if (snapshot.hasChild("picture")) {
                        String picturePath = snapshot.child("picture").getValue().toString();
                        Picasso.get()
                                .load(picturePath)
                                .fit()
                                .centerCrop()
                                .into(profilePic);
                    } else {
                        profilePic.setImageResource(R.drawable.ic_profile_picture_register);
                    }

                    textName.setText(name);
                    textGroup.setText(groupName);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private boolean isUsernameChanged(){
        return !_NAME.equals(textName.getText().toString());

    }
}
