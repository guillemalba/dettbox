package com.glasswork.dettbox.ui.tasks;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.glasswork.dettbox.R;
import com.glasswork.dettbox.ui.ranking.GrupListFragment;
import com.glasswork.dettbox.ui.ranking.MainRankingFragment;
import com.glasswork.dettbox.ui.ranking.NoGroupFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainTasksFragment extends Fragment {

    private static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    private EditText etTaskDescription;
    private Spinner spinnerMenu;
    private Button btnAddTask;
    private View vAddTask;

    private Button btnDay;
    private Button btnWeek;
    private Button btnMonth;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        btnDay = getActivity().findViewById(R.id.button_day);
        btnWeek = getActivity().findViewById(R.id.button_week);
        btnMonth = getActivity().findViewById(R.id.button_month);

        btnDay.setVisibility(View.GONE);
        btnWeek.setVisibility(View.GONE);
        btnMonth.setVisibility(View.GONE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String fragment = prefs.getString("fragment_tasks", "null");
        Boolean yourLocked = prefs.getBoolean(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupStatus", true);

        // if the user is not in a group or not change fragment to display
        if (yourLocked) {
            Fragment childFragment = new NoGroupTasksFragment();
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, childFragment).commit();
        } else {
            switch (fragment) {
                case "verify_task":
                    getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, new VerifyTasksFragment()).addToBackStack(null).commit();
                    break;
                case "add_task":
                    break;
                default:
                    getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, new MenuTasksFragment()).addToBackStack(null).commit();
                    break;
            }
        }



        /*if (veryfyTask) {
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, new VerifyTasksFragment()).addToBackStack(null).commit();
        } else {
            prefs.edit().putBoolean("verify_task", true).commit();

        }*/





        /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Boolean yourLocked = prefs.getBoolean(FirebaseAuth.getInstance().getCurrentUser().getUid(), true);

        // if the user is not in a group or not change fragment to display
        if (yourLocked) {
            Fragment childFragment = new NoGroupFragment();
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, childFragment).commit();
        } else {
            Fragment childFragment = new GrupListFragment();
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, childFragment).commit();
        }*/
        return view;
    }


}
