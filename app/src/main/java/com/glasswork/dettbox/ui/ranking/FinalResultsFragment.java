package com.glasswork.dettbox.ui.ranking;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.glasswork.dettbox.R;
import com.glasswork.dettbox.model.User;
import com.glasswork.dettbox.model.UserRanking;
import com.glasswork.dettbox.ui.tasks.ManageRewardFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class FinalResultsFragment extends Fragment {

    private static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";
    private UserRanking userRankingLoser;
    private UserRanking userRankingWinner;
    private CountDownTimer mCountDownTimer;

    private TextView tvPositionWinner;
    private TextView tvNameWinner;
    private TextView tvTimeWinner;
    private TextView tvReward;
    private Button btnChooseReward;

    private TextView tvPositionLoser;
    private TextView tvNameLoser;
    private TextView tvTimeLoser;
    private TextView tvPunishment;
    private Button btnChoosePunishment;

    private TextView tvSeasonTitle;

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    private String prefsGroupName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_final_results, container, false);

        prefsGroupName = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");

        readIds(view);
        readWinner();
        readLoser();

        setSeasonTitle();

        Button btn = view.findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, new MainRankingFragment());
                ft.addToBackStack("FinalResultsFragment222");
                ft.commit();
            }
        });

        btnChooseReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseRewardDialog();
            }
        });

        btnChoosePunishment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePunishmentDialog();
            }
        });

        FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Groups")
                .child(prefsGroupName)
                .child("FinalResults")
                .child("Winner")
                .child("reward")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            tvReward.setText(snapshot.getValue().toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Groups")
                .child(prefsGroupName)
                .child("FinalResults")
                .child("Loser")
                .child("punishment")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            tvPunishment.setText(snapshot.getValue().toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        /*mCountDownTimer = new CountDownTimer(60000, 1000) {
            StringBuilder time = new StringBuilder();

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                FragmentTransaction ft = ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, new MainRankingFragment());
                ft.addToBackStack("FinalResultsFragment222");
                ft.commit();
            }
        }.start();*/

        return view;
    }

    private void setSeasonTitle() {
        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH) + 1;
        String monthName;
        switch (currentMonth) {
            case 1:
                monthName = "December";
                break;
            case 2:
                monthName = "January";
                break;
            case 3:
                monthName = "February";
                break;
            case 4:
                monthName = "March";
                break;
            case 5:
                monthName = "April";
                break;
            case 6:
                monthName = "May";
                break;
            case 7:
                monthName = "June";
                break;
            case 8:
                monthName = "July";
                break;
            case 9:
                monthName = "August";
                break;
            case 10:
                monthName = "September";
                break;
            case 11:
                monthName = "October";
                break;
            case 12:
                monthName = "November";
                break;
            default:
                monthName = "";

        }
        tvSeasonTitle.setText(monthName + " results");
    }

    public void choosePunishmentDialog() {
        dialogBuilder = new AlertDialog.Builder(getContext());
        final View choosePunishmentView = getLayoutInflater().inflate(R.layout.popup_choose_punishment, null);

        dialogBuilder.setView(choosePunishmentView);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        recyclerView = choosePunishmentView.findViewById(R.id.choose_punishment_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setFirebaseAdapter();
        fetchPunishments();
        firebaseRecyclerAdapter.startListening();
    }

    private void fetchPunishments() {

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

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<HashMap<String, String>, FinalResultsFragment.MyViewHolder>(options) {
            @NonNull
            @Override
            public FinalResultsFragment.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_show_reward_punishment, parent, false);
                return new FinalResultsFragment.MyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FinalResultsFragment.MyViewHolder myViewHolder, int i, @NonNull HashMap<String, String> map) {
                for(Map.Entry map1  :  map.entrySet() ){
                    if (map1.getKey().toString().contains("personalized")) {
                        myViewHolder.setIvColorPersonalized();
                    } else {
                        myViewHolder.setIvColorDefault();
                    }
                    myViewHolder.setTvTitle(i + 1 + ". " + map1.getValue());
                    myViewHolder.setPunishment(map1.getValue().toString());
                }
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public void chooseRewardDialog() {
        dialogBuilder = new AlertDialog.Builder(getContext());
        final View chooseRewardView = getLayoutInflater().inflate(R.layout.popup_choose_reward, null);

        dialogBuilder.setView(chooseRewardView);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        recyclerView = chooseRewardView.findViewById(R.id.choose_rewards_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setFirebaseAdapter();
        fetchRewards();
        firebaseRecyclerAdapter.startListening();
    }

    public void setFirebaseAdapter() {

        // Reverses the order on retrieving data from firebase
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void fetchRewards() {

        Query query = FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Groups")
                .child(prefsGroupName)
                .child("Rewards");

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

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<HashMap<String, String>, FinalResultsFragment.MyViewHolder>(options) {
            @NonNull
            @Override
            public FinalResultsFragment.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_show_reward_punishment, parent, false);
                return new FinalResultsFragment.MyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FinalResultsFragment.MyViewHolder myViewHolder, int i, @NonNull HashMap<String, String> map) {
                for(Map.Entry map1  :  map.entrySet() ){
                    if (map1.getKey().toString().contains("personalized")) {
                        myViewHolder.setIvColorPersonalized();
                    } else {
                        myViewHolder.setIvColorDefault();
                    }
                    myViewHolder.setTvTitle(i + 1 + ". " + map1.getValue());
                    myViewHolder.setReward(map1.getValue().toString());
                }
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private ImageView imageView;
        private CardView cardView;

        public MyViewHolder(@NonNull View view) {
            super(view);
            tvTitle = view.findViewById(R.id.title);
            imageView = view.findViewById(R.id.iv_tag_color);
            cardView = view.findViewById(R.id.card_view);
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

        public void setPunishment(String punishment) {
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tvPunishment.setText(punishment);
                    dialog.dismiss();
                    FirebaseDatabase.getInstance(FIREBASE_LINK)
                            .getReference("Groups")
                            .child(prefsGroupName)
                            .child("FinalResults")
                            .child("Loser")
                            .child("punishment")
                            .setValue(punishment);
                }
            });
        }
        
        public void setReward(String reward) {
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tvReward.setText(reward);
                    dialog.dismiss();
                    FirebaseDatabase.getInstance(FIREBASE_LINK)
                            .getReference("Groups")
                            .child(prefsGroupName)
                            .child("FinalResults")
                            .child("Winner")
                            .child("reward")
                            .setValue(reward);
                }
            });
        }

    }

    public void readIds(View v) {
        tvPositionLoser = v.findViewById(R.id.tv_position_loser);
        tvNameLoser = v.findViewById(R.id.tv_name_loser);
        tvTimeLoser = v.findViewById(R.id.tv_time_loser);
        tvPunishment = v.findViewById(R.id.tv_punishment);
        btnChoosePunishment = v.findViewById(R.id.btn_choose_punishment);

        tvPositionWinner = v.findViewById(R.id.tv_position_winner);
        tvNameWinner = v.findViewById(R.id.tv_name_winner);
        tvTimeWinner = v.findViewById(R.id.tv_time_winner);
        tvReward = v.findViewById(R.id.tv_reward);
        btnChooseReward = v.findViewById(R.id.btn_choose_reward);

        tvSeasonTitle = v.findViewById(R.id.season_title);
    }

    public void readWinner() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String prefsGroupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");
        FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Groups")
                .child(prefsGroupName)
                .child("FinalResults")
                .child("Winner")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String id = snapshot.child("id").getValue().toString();
                            String name = snapshot.child("name").getValue().toString();
                            String position = snapshot.child("position").getValue().toString();
                            String time = snapshot.child("time").getValue().toString();
                            tvPositionWinner.setText(position);
                            tvNameWinner.setText(name);
                            tvTimeWinner.setText(time);

                            if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(id)) {
                                btnChoosePunishment.setVisibility(View.GONE);
                                btnChooseReward.setVisibility(View.GONE);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public void readLoser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String prefsGroupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");
        FirebaseDatabase.getInstance(FIREBASE_LINK)
                .getReference("Groups")
                .child(prefsGroupName)
                .child("FinalResults")
                .child("Loser")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue().toString();
                            String position = snapshot.child("position").getValue().toString();
                            String time = snapshot.child("time").getValue().toString();
                            tvPositionLoser.setText(position);
                            tvNameLoser.setText(name);
                            tvTimeLoser.setText(time);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

}