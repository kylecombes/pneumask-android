package com.kylecombes.micrepeater;

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

public class WelcomeWizardInfoPageFragment extends Fragment {
    private static final String ARG_PAGE_INDEX = "PAGE_INDEX";
    private static final String ARG_LAYOUT_REF = "ARG_LAYOUT_REF";
    private int mPageIndex;
    private int mLayoutRef;

    public static WelcomeWizardInfoPageFragment newInstance(int counter, int layoutFile) {
        WelcomeWizardInfoPageFragment fragment = new WelcomeWizardInfoPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE_INDEX, counter);
        args.putInt(ARG_LAYOUT_REF, layoutFile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPageIndex = getArguments().getInt(ARG_PAGE_INDEX);
            mLayoutRef = getArguments().getInt(ARG_LAYOUT_REF);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(mLayoutRef, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mPageIndex == 4) {
            // Set up firebase switch and useFirebase shared preference
            Switch useFirebaseSwitch = view.findViewById(R.id.data_collection_consent_switch);
            // By default, set useFirebase to true
            SharedPreferences preferences = getActivity()
                    .getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE);
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
                    SharedPreferences preferences = getActivity()
                            .getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edt = preferences.edit();
                    edt.putBoolean("useFirebase", isChecked);
                    edt.apply();
                }
            });
        }
    }
}

