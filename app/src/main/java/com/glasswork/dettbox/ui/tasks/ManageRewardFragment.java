package com.glasswork.dettbox.ui.tasks;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.glasswork.dettbox.Messages;
import com.glasswork.dettbox.R;
import com.glasswork.dettbox.model.ActiveTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageRewardFragment extends Fragment {

    private static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";
    private ArrayList<String> listRewards;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    private TextView tvTitlePage;
    private View vIconPage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manage_reward, container, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putString("fragment_tasks", "null").commit();

        listRewards = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recycle_rewards_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setFirebaseAdapter();
        fetch();

        return view;
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
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitleOnScreen();
    }

    public void setTitleOnScreen() {
        tvTitlePage = getActivity().findViewById(R.id.tvTitle);
        vIconPage = getActivity().findViewById(R.id.icon_view);
        tvTitlePage.setText("Rewards");
        vIconPage.setBackground(getContext().getDrawable(R.drawable.ic_task_verified2));
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
                .child("Rewards");

        FirebaseRecyclerOptions<String> options = new FirebaseRecyclerOptions.Builder<String>()
                .setQuery(query, new SnapshotParser<String>() {
                    @NonNull
                    @Override
                    public String parseSnapshot(@NonNull DataSnapshot snapshot) {
                        return snapshot.child("title").getValue().toString();
                    }
                })
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<String, ManageRewardFragment.MyViewHolder>(options) {
            @NonNull
            @Override
            public ManageRewardFragment.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_reward_punishment, parent, false);
                return new ManageRewardFragment.MyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ManageRewardFragment.MyViewHolder myViewHolder, int i, @NonNull String title) {
                myViewHolder.setTvTitle(title);
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;

        public MyViewHolder(@NonNull View view) {
            super(view);
            tvTitle = view.findViewById(R.id.title);
        }

        public void setTvTitle(String string) {
            tvTitle.setText(string);
        }

    }
}