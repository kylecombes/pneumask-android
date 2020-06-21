package com.kylecombes.micrepeater.ui.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.kylecombes.micrepeater.R;

public class MicDeviceStatusTile extends ConstraintLayout {
    public MicDeviceStatusTile(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.device_status_tile, this, true);
    }
}
