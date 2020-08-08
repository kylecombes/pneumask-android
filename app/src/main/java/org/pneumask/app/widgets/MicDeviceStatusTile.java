package org.pneumask.app.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.pneumask.app.R;

public class MicDeviceStatusTile extends ConstraintLayout {
    public MicDeviceStatusTile(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.bluetooth_device_status_tile, this, true);
    }
}
