package com.kylecombes.micrepeater;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class ScreenSlidePageFragment extends Fragment {
    private static final String ARG_COUNT = "param1";
    private Integer counter;
    private int[] layout_files = {
            R.layout.welcome1, R.layout.welcome2, R.layout.welcome3, R.layout.welcome4, R.layout.welcome5, R.layout.welcome6,
    };

    public static ScreenSlidePageFragment newInstance(Integer counter) {
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COUNT, counter);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            counter = getArguments().getInt(ARG_COUNT);
        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(layout_files[counter], container, false);
    }
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(counter == 4){
            Switch datareporting = view.findViewById(R.id.data_switch);
            //by default, set reportdata to true
            SharedPreferences preferences = getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE);
            SharedPreferences.Editor edt = preferences.edit();
            edt.putBoolean("reportdata", true);
            edt.commit();

            datareporting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //set reportdata to what the user sets it to
                    SharedPreferences preferences = getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edt = preferences.edit();
                    edt.putBoolean("reportdata", isChecked);
                    edt.commit();
                }
            });
        }
    }
}

