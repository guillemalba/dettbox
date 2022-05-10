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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.glasswork.dettbox.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ManagePunishmentFragment extends Fragment {

    private static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";
    private ArrayList<String> listRewards;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    private TextView tvTitlePage;
    private View vIconPage;

    private Button btnAddPunishment;

    private EditText etTitle;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Button btnAdd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manage_punishment, container, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putString("fragment_tasks", "null").commit();

        btnAddPunishment = getActivity().findViewById(R.id.btnAddPunishment);
        btnAddPunishment.setVisibility(View.VISIBLE);
        btnAddPunishment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPunishmentDialog();
            }
        });

        listRewards = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recycle_punishments_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setFirebaseAdapter();
        fetch();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        btnAddPunishment.setVisibility(View.GONE);
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
        btnAddPunishment.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitleOnScreen();
        btnAddPunishment.setVisibility(View.VISIBLE);
    }

    public void setTitleOnScreen() {
        tvTitlePage = getActivity().findViewById(R.id.tvTitle);
        vIconPage = getActivity().findViewById(R.id.icon_view);
        tvTitlePage.setText(R.string.manage_pun);
        vIconPage.setBackground(getContext().getDrawable(R.drawable.ic_punishment_white));
    }

    public void addPunishmentDialog() {
        dialogBuilder = new AlertDialog.Builder(getContext());
        final View addTaskView = getLayoutInflater().inflate(R.layout.popup_add_punishment, null);

        dialogBuilder.setView(addTaskView);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        etTitle = addTaskView.findViewById(R.id.inputPunishmentTitle);
        btnAdd = addTaskView.findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                String groupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");

                String punishmentTitle = etTitle.getText().toString();

                FirebaseDatabase.getInstance(FIREBASE_LINK)
                        .getReference("Groups")
                        .child(groupName)
                        .child("Punishments")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                long numPunishments = snapshot.getChildrenCount();
                                FirebaseDatabase.getInstance(FIREBASE_LINK)
                                        .getReference("Groups")
                                        .child(groupName)
                                        .child("Punishments")
                                        .child("personalized-" + numPunishments)
                                        .child("title")
                                        .setValue(punishmentTitle);
                                Toast.makeText(getContext(), "Punishment successfully added!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                dialog.dismiss();
            }
        });
    }

    public void setFirebaseAdapter() {

        // Reverses the order on retrieving data from firebase
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void fetch() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String prefsGroupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");

        Query query = FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Groups")
                .child(prefsGroupName)
                .child("Punishments");

        FirebaseRecyclerOptions<HashMap<String, String>> options = new FirebaseRecyclerOptions.Builder<HashMap<String, String>>()
                .setQuery(query, new SnapshotParser<HashMap<String, String>>() {
                    @NonNull
                    @Override
                    public HashMap<String, String> parseSnapshot(@NonNull DataSnapshot snapshot) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(snapshot.getKey(), snapshot.child("title").getValue().toString());
                        return map;
                    }
                })
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<HashMap<String, String>, ManagePunishmentFragment.MyViewHolder>(options) {
            @NonNull
            @Override
            public ManagePunishmentFragment.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_reward_punishment, parent, false);
                return new ManagePunishmentFragment.MyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ManagePunishmentFragment.MyViewHolder myViewHolder, int i, @NonNull HashMap<String, String> map) {
                for(Map.Entry map1  :  map.entrySet() ){
                    if (map1.getKey().toString().contains("personalized")) {
                        myViewHolder.setIvColorPersonalized();
                    } else {
                        myViewHolder.setIvColorDefault();
                    }
                    myViewHolder.setTvTitle(i + 1 + ". " + map1.getValue());
                    myViewHolder.setBtnDelete(map1.getKey().toString());
                }
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private ImageView imageView;
        private Button btnDelete;

        public MyViewHolder(@NonNull View view) {
            super(view);
            tvTitle = view.findViewById(R.id.title);
            imageView = view.findViewById(R.id.iv_tag_color);
            btnDelete = view.findViewById(R.id.btn_delete);
        }

        public void setBtnDelete(String punishmentId) {
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    String prefsGroupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");
                    FirebaseDatabase.getInstance(FIREBASE_LINK)
                            .getReference("Groups")
                            .child(prefsGroupName)
                            .child("Punishments")
                            .child(punishmentId)
                            .removeValue();
                    Toast.makeText(getContext(), "Punishment deleted!", Toast.LENGTH_SHORT).show();
                }

            });
        }

        public void setTvTitle(String string) {
            tvTitle.setText(string);
        }

        public void setIvColorPersonalized() {
            imageView.setColorFilter(getResources().getColor(R.color.colorPersonalizedReward));
        }

        public void setIvColorDefault() {
            imageView.setColorFilter(getResources().getColor(R.color.colorDefaultReward));
        }

    }
}