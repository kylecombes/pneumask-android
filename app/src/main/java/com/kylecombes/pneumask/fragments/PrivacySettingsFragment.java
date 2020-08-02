package com.kylecombes.pneumask.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kylecombes.pneumask.R;

public class PrivacySettingsFragment extends Fragment {

    public static PrivacySettingsFragment newInstance() {
        return new PrivacySettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_privacy_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set up firebase switch and useFirebase shared preference
        Switch useFirebaseSwitch = view.findViewById(R.id.data_collection_consent_switch);
        // By default, set useFirebase to true
        SharedPreferences preferences = getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE);
        if (preferences.contains("useFirebase")) {
            useFirebaseSwitch.setChecked(preferences.getBoolean("useFirebase", true));
        } else {
            SharedPreferences.Editor edt = preferences.edit();
            edt.putBoolean("useFirebase", true);
            edt.apply();
        }

        useFirebaseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Set useFirebase to what the user sets it to
                SharedPreferences preferences = getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE);
                SharedPreferences.Editor edt = preferences.edit();
                edt.putBoolean("useFirebase", isChecked);
                edt.apply();
            }
        });
    }
}

