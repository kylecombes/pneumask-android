package com.kylecombes.micrepeater.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.kylecombes.micrepeater.R;

public class AmplifyingControlTile extends ConstraintLayout {

    private Button mStartStopButton;
    private TextView mStatusTextView;

    private int COLOR_PRIMARY = getResources().getColor(R.color.colorPrimary);
    private int COLOR_RED = getResources().getColor(R.color.red);

    public AmplifyingControlTile(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.amplifying_control_tile, this, true);

        mStartStopButton = findViewById(R.id.amplifying_control_start_stop_button);
        mStatusTextView = findViewById(R.id.amplifying_control_tile_status_text);
    }

    public void setAmplifyingActive(boolean isActive) {
        if (isActive) {
            mStartStopButton.setText(R.string.stop);
            mStatusTextView.setText(R.string.amplification_active);
            mStatusTextView.setTextColor(COLOR_RED);
        } else {
            mStatusTextView.setText(R.string.off);
            mStartStopButton.setText(R.string.start);
            mStatusTextView.setTextColor(COLOR_PRIMARY);
        }
    }
}
