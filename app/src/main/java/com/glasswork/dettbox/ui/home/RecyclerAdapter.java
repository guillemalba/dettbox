package com.glasswork.dettbox.ui.home;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.glasswork.dettbox.R;
import com.glasswork.dettbox.model.AppItem;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private ArrayList<AppItem> appList;
    private Context context;

    public RecyclerAdapter(ArrayList<AppItem> appList) {
        this.appList = appList;
    }

    public RecyclerAdapter() {

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAppName;
        private TextView tvAppTime;
        private ImageView ivAppIcon;

        public MyViewHolder(final View view) {
            super(view);
            tvAppName = view.findViewById(R.id.app_name);
            tvAppTime = view.findViewById(R.id.app_time);
            ivAppIcon = view.findViewById(R.id.app_icon);
        }
    }

    @NonNull
    @Override
    public RecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_app_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.MyViewHolder holder, int position) {
        String name = appList.get(position).getAppName();
        String time = appList.get(position).getAppTimeUsed();
        Drawable drawableImage = appList.get(position).getDrawableIcon();

        holder.tvAppName.setText(name);
        holder.tvAppTime.setText(time);
        holder.ivAppIcon.setImageDrawable(drawableImage);

    }

    @Override
    public int getItemCount() {
        return appList.size();
    }
}
