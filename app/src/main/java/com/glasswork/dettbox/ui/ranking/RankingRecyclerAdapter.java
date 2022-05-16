package com.glasswork.dettbox.ui.ranking;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.glasswork.dettbox.R;
import com.glasswork.dettbox.model.UserRanking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RankingRecyclerAdapter extends RecyclerView.Adapter<RankingRecyclerAdapter.MyViewHolder>{

    private static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";

    private ArrayList<UserRanking> userRankingArrayList;
    private Context context;

    public RankingRecyclerAdapter(ArrayList<UserRanking> userRankingArrayList, Context context) {
        this.userRankingArrayList = userRankingArrayList;
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvPosition;
        private TextView tvTime;
        private View viewBackground;
        private View viewBackgroundBorder;
        private View viewBackgroundPosition4;

        public MyViewHolder(final View view) {
            super(view);
            tvName = view.findViewById(R.id.textName);
            tvPosition = view.findViewById(R.id.textPosition);
            tvTime = view.findViewById(R.id.textTime);
            viewBackground = view.findViewById(R.id.viewBackground);
            viewBackgroundBorder = view.findViewById(R.id.viewBackgroundBorder);
            viewBackgroundPosition4 = view.findViewById(R.id.viewBackgroundPosition4);

        }
    }

    @NonNull
    @Override
    public RankingRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_ranking_item, parent, false);
        return new RankingRecyclerAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingRecyclerAdapter.MyViewHolder holder, int position) {
        String id = userRankingArrayList.get(position).getId();
        String name = userRankingArrayList.get(position).getName();
        String pos = userRankingArrayList.get(position).getPosition();
        String time = userRankingArrayList.get(position).getTime();

        holder.tvName.setText(name);
        holder.tvTime.setText(time);

        holder.viewBackgroundPosition4.setBackgroundResource(R.drawable.background_position);
        holder.tvPosition.setText(pos);
        int listSize = userRankingArrayList.size();
        if (position == listSize-1) {
            holder.viewBackgroundPosition4.setBackgroundResource(R.drawable.background_position_loser);
        }
        if (position == 0) {
            holder.viewBackgroundPosition4.setBackgroundResource(R.drawable.background_position_winner);

        }

        holder.viewBackgroundBorder.setBackgroundResource(R.drawable.input_bg);
        if (id.equals(FirebaseAuth.getInstance().getUid())) {
            holder.viewBackgroundBorder.setBackgroundResource(R.drawable.ranking_my_position);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String prefsGroupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");

        if (userRankingArrayList.get(position).getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            String attribute;
            switch (position) {
                case 0:
                    attribute = "st";
                    break;
                case 1:
                    attribute = "nd";
                    break;
                case 2:
                    attribute = "rd";
                    break;
                default:
                    attribute = "th";
                    break;
            }
            FirebaseDatabase.getInstance(FIREBASE_LINK)
                    .getReference("Groups")
                    .child(prefsGroupName)
                    .child("Users")
                    .child(userRankingArrayList.get(position).getId())
                    .child("rankingPosition")
                    .setValue(userRankingArrayList.get(position).getPosition() + attribute);
        }
    }

    @Override
    public int getItemCount() {
        return userRankingArrayList.size();
    }
}
