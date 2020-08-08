package org.pneumask.app.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Spinner;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.pneumask.app.R;

public class AudioOutputControlTile extends ConstraintLayout {

    private Spinner audioDropdown;

    public AudioOutputControlTile(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.floating_dropdown_box, this, true);

        audioDropdown = findViewById(R.id.audio_output_dropdown);
    }

}
