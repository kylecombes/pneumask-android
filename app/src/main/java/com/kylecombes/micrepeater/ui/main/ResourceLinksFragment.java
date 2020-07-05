package com.kylecombes.micrepeater.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.kylecombes.micrepeater.PrivacySettingsActivity;
import com.kylecombes.micrepeater.R;

public class ResourceLinksFragment extends Fragment {

    static ResourceLinksFragment newInstance() {
        return new ResourceLinksFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pneumask_resources, container, false);
        root.findViewById(R.id.app_resource_link_privacy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PrivacySettingsActivity.class));
            }
        });
        return root;
    }
}