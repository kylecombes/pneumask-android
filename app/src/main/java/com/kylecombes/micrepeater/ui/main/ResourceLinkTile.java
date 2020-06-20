package com.kylecombes.micrepeater.ui.main;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.kylecombes.micrepeater.R;
import com.kylecombes.micrepeater.ResourceLink;

public class ResourceLinkTile extends ConstraintLayout {

    private TextView mTextView;
    private ImageView mIcon;

    public ResourceLinkTile(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.resource_link_tile, this, true);

        // Locate the views
        mTextView = findViewById(R.id.resource_link_name_tv);
        mIcon = findViewById(R.id.resource_link_iv);

    }

    public ResourceLinkTile(Context context, ResourceLink resourceLink) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.resource_link_tile, this, true);

        initViews(context, resourceLink);
    }

    private void initViews(Context context, AttributeSet attrs) {
        findViews();

        // Apply the attributes to the views
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ResourceLinkTile,
                0, 0);
        try {
            mTextView.setText(a.getText(R.styleable.ResourceLinkTile_link_title));
            mIcon.setImageDrawable(a.getDrawable(R.styleable.ResourceLinkTile_tile_image));

            CharSequence linkUrl = a.getText(R.styleable.ResourceLinkTile_link_url);
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        } finally {
            a.recycle();
        }
    }

    private void initViews(Context context, ResourceLink resourceLink) {
        findViews();

        mTextView.setText(resourceLink.getTextResId());
        Integer drawableResId = resourceLink.getImageResId();
        if (drawableResId != null) {
            mIcon.setImageDrawable(context.getResources().getDrawable(drawableResId));
        }
    }

    private void findViews() {
        // Locate the views
        mTextView = findViewById(R.id.resource_link_name_tv);
        mIcon = findViewById(R.id.resource_link_iv);
    }

}
