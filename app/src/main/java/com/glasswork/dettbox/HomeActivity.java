package com.glasswork.dettbox;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";

    private Button btnLogout;
    private Button btnRed;
    private Button btnGreen;
    private Button btnBlue;

    private Boolean buttonClicked = false;

    private FirebaseAuth mAuth;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // to remove top navbar
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorDark));*/

        btnRed = findViewById(R.id.btnRed);
        btnGreen = findViewById(R.id.btnGreen);
        btnBlue = findViewById(R.id.btnBlue);

        if (getColor("btnRed") != Color.GRAY) {
            btnRed.setBackgroundColor(getColor("btnRed"));
        }

        btnRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked = true;
                DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("led_boolean_R");

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String data = snapshot.getValue().toString();
                            if (buttonClicked) {
                                buttonClicked = false;

                                switch (data) {
                                    case "False":
                                        btnRed.setBackgroundColor(Color.RED);
                                        storeButton(Color.RED, "btnRed");

                                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                .getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("led_boolean_R")
                                                .setValue("True");
                                        break;
                                    case "True":
                                        btnRed.setBackgroundColor(Color.WHITE);
                                        storeButton(Color.WHITE, "btnRed");

                                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                .getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("led_boolean_R")
                                                .setValue("False");
                                        break;

                                }
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, "onCancelled() method activated! in Home Activity!", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        if (getColor("btnGreen") != Color.GRAY) {
            btnGreen.setBackgroundColor(getColor("btnGreen"));
        }

        btnGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked = true;
                DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("led_boolean_G");

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String data = snapshot.getValue().toString();
                            if (buttonClicked) {
                                buttonClicked = false;

                                switch (data) {
                                    case "False":
                                        btnGreen.setBackgroundColor(Color.GREEN);
                                        storeButton(Color.GREEN, "btnGreen");

                                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                .getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("led_boolean_G")
                                                .setValue("True");
                                        break;
                                    case "True":
                                        btnGreen.setBackgroundColor(Color.WHITE);
                                        storeButton(Color.WHITE, "btnGreen");

                                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                .getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("led_boolean_G")
                                                .setValue("False");
                                        break;

                                }
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, "onCancelled() method activated! in Home Activity!", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        if (getColor("btnBlue") != Color.GRAY) {
            btnBlue.setBackgroundColor(getColor("btnBlue"));
        }

        btnBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked = true;
                DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("led_boolean_B");

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String data = snapshot.getValue().toString();
                            if (buttonClicked) {
                                buttonClicked = false;

                                switch (data) {
                                    case "False":
                                        btnBlue.setBackgroundColor(Color.BLUE);
                                        storeButton(Color.BLUE, "btnBlue");

                                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                .getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("led_boolean_B")
                                                .setValue("True");
                                        break;
                                    case "True":
                                        btnBlue.setBackgroundColor(Color.WHITE);
                                        storeButton(Color.WHITE, "btnBlue");

                                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                .getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("led_boolean_B")
                                                .setValue("False");
                                        break;

                                }
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, "onCancelled() method activated! in Home Activity!", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        mAuth = FirebaseAuth.getInstance();
        btnLogout = findViewById(R.id.btnlogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }

    public void storeButton(int color, String btnName) {
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor save = sharedPreferences.edit();
        /*Toast.makeText(HomeActivity.this, btnName + " --> " + color , Toast.LENGTH_SHORT).show();*/
        save.putInt(btnName, color);
        save.apply();
    }

    public int getColor (String btnName) {
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        int selectedColor = sharedPreferences.getInt(btnName, Color.WHITE);
        return selectedColor;
    }

}