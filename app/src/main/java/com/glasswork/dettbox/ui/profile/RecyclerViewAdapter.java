package com.glasswork.dettbox.ui.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.glasswork.dettbox.Messages;
import com.glasswork.dettbox.R;
import com.glasswork.dettbox.ui.home.HomeFragment;
import com.glasswork.dettbox.ui.home.RecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private Context context;
    private AlertDialog dialog;
    private List<String> listImages;

    public RecyclerViewAdapter(Context context, AlertDialog dialog, List<String> listImages) {
        this.context = context;
        this.dialog = dialog;
        this.listImages = listImages;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_pic, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(itemView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get()
                .load(listImages.get(position))
                .fit()
                .centerCrop()
                .into(holder.ivProfilePic);

        String imagePath;
        switch (position) {
            case 0:
                imagePath = Messages.CARA1_CONTENT.toString();
                break;
            case 1:
                imagePath = Messages.CARA1_MIG.toString();
                break;
            case 2:
                imagePath = Messages.CARA1_TRIST.toString();
                break;
            case 3:
                imagePath = Messages.CARA2_CONTENT.toString();
                break;
            case 4:
                imagePath = Messages.CARA2_MIG.toString();
                break;
            case 5:
                imagePath = Messages.CARA2_TRIST.toString();
                break;
            default:
                imagePath = "null";
                break;
        }

        holder.ivProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance(Messages.FIREBASE_LINK.toString())
                        .getReference("Users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("picture")
                        .setValue(imagePath);
                dialog.dismiss();
                refreshFragment();
                Toast.makeText(v.getContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listImages.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivProfilePic;

        public MyViewHolder(final View view) {
            super(view);
            ivProfilePic = view.findViewById(R.id.imageview_profile_pic);
        }
    }

    public void refreshFragment() {
        FragmentTransaction ft = ((FragmentActivity)context).getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new ProfileFragment());
        ft.commit();
    }
}
