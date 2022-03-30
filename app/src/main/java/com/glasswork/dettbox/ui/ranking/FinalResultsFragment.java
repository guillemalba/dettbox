package com.glasswork.dettbox.ui.ranking;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.glasswork.dettbox.R;


public class FinalResultsFragment extends Fragment {

    CountDownTimer mCountDownTimer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_final_results, container, false);

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


        mCountDownTimer = new CountDownTimer(10000, 1000) {
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
        }.start();

        return view;
    }
}