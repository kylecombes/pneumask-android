package com.kylecombes.micrepeater.ui.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.kylecombes.micrepeater.R;

public class FloatingTileBox extends ConstraintLayout {
    public FloatingTileBox(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.floating_tile_box, this, true);
    }
}
