package com.glasswork.dettbox.ui.ranking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.glasswork.dettbox.R;
import com.glasswork.dettbox.model.UserRanking;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class RankingRecyclerAdapter extends RecyclerView.Adapter<RankingRecyclerAdapter.MyViewHolder>{

    private ArrayList<UserRanking> userRankingArrayList;
    private Context context;

    public RankingRecyclerAdapter(ArrayList<UserRanking> userRankingArrayList) {
        this.userRankingArrayList = userRankingArrayList;
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

    }

    @Override
    public int getItemCount() {
        return userRankingArrayList.size();
    }
}
