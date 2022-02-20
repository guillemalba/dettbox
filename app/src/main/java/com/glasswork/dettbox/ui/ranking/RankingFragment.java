package com.glasswork.dettbox.ui.ranking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.glasswork.dettbox.R;
import com.glasswork.dettbox.ui.profile.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RankingFragment extends Fragment {

    Button btn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        // if the user is not in a group or not change fragment to display
        if (true) {
            Fragment childFragment = new NoGroupFragment();
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, childFragment).commit();
        } else {
            Fragment childFragment = new GrupListFragment();
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, childFragment).commit();
        }


        return view;
    }


}
