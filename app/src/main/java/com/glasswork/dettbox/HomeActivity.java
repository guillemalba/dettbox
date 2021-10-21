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
    private Button btn;
    private Boolean buttonClicked = false;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private boolean isFirstTime = true;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btn = findViewById(R.id.button);

        if (getColor() != Color.GRAY) {
            btn.setBackgroundColor(getColor());
            switch (getColor()) {
                case Color.RED:
                    btn.setText("FALSE");
                    break;
                case Color.GREEN:
                    btn.setText("TRUE");
                    break;
            }
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked = true;
                DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("led_boolean");

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String data = snapshot.getValue().toString();
                            if (buttonClicked) {
                                buttonClicked = false;

                                switch (data) {
                                    case "true":
                                        btn.setText("False");
                                        btn.setBackgroundColor(Color.RED);
                                        storeButton(Color.RED, "false");

                                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                .getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("led_boolean")
                                                .setValue(false);
                                        break;
                                    case "false":
                                        btn.setText("True");
                                        btn.setBackgroundColor(Color.GREEN);
                                        storeButton(Color.GREEN, "true");

                                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                                .getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("led_boolean")
                                                .setValue(true);
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

    public void storeButton(int color, String status) {
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor save = sharedPreferences.edit();
        save.putString("button_status", status);
        save.putInt("button_color", color);
        save.apply();
    }

    public int getColor () {
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        int selectedColor = sharedPreferences.getInt("button_color", Color.GRAY);
        return selectedColor;
    }

}