package com.glasswork.dettbox;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
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

    private Button btn;
    private Boolean buttonClicked = false;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked = true;
                DatabaseReference reference = FirebaseDatabase.getInstance("https://dettbox-default-rtdb.europe-west1.firebasedatabase.app")
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
                                        FirebaseDatabase.getInstance("https://dettbox-default-rtdb.europe-west1.firebasedatabase.app")
                                                .getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("led_boolean")
                                                .setValue(false);/*.addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(HomeActivity.this, "Boolean saved into our database!", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(HomeActivity.this, "Failed on saving to our database!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });*/
                                        break;
                                    case "false":
                                        btn.setText("True");
                                        btn.setBackgroundColor(Color.GREEN);
                                        FirebaseDatabase.getInstance("https://dettbox-default-rtdb.europe-west1.firebasedatabase.app")
                                                .getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("led_boolean")
                                                .setValue(true);/*.addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(HomeActivity.this, "Boolean saved into our database!", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(HomeActivity.this, "Failed on saving to our database!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });*/
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
    }
}