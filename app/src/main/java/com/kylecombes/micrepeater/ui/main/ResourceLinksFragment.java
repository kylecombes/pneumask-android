package com.kylecombes.micrepeater.ui.main;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.kylecombes.micrepeater.R;
import com.kylecombes.micrepeater.ResourceLink;

import java.util.List;

public class ResourceLinksFragment extends Fragment {

    static ResourceLinksFragment newInstance() {
        return new ResourceLinksFragment();
    }

    static ResourceLink[] resources = {
            new ResourceLink(R.string.start, R.drawable.bluetooth, Uri.parse("https://www.google.com"))
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pneumask_resources, container, false);

        ConstraintLayout layout = (ConstraintLayout)root;
        Context context = getActivity();
        for (ResourceLink resourceLink : resources) {
            ResourceLinkTile tile = new ResourceLinkTile(context, resourceLink);
            layout.addView(tile);
        }
        return root;
    }


}