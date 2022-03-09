package com.glasswork.dettbox.ui.tasks;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.glasswork.dettbox.R;
import com.glasswork.dettbox.ui.ranking.NoGroupFragment;
import com.google.firebase.auth.FirebaseAuth;


public class VerifyTasksFragment extends Fragment {


    private View buttonView;
    private TextView tvTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_verify_tasks, container, false);

        setTitleOnScreen();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putString("fragment_tasks", "null").commit();

        buttonView = view.findViewById(R.id.button);
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Fragment fragment = new MainTasksFragment();
                //replacing the fragment
                if (fragment != null) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    prefs.edit().putBoolean("verify_task", false).commit();

                    FragmentTransaction ft = ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.fragment_container, fragment);
                    ft.addToBackStack("back");
                    ft.commit();
                }*/

            }
        });

        return view;
    }

    public void setTitleOnScreen() {
        tvTitle = getActivity().findViewById(R.id.tvTitle);
        tvTitle.setText("Verify tasks");
    }

}