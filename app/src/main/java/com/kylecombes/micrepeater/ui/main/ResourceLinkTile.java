package com.kylecombes.micrepeater.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.kylecombes.micrepeater.R;

public class ResourceLinkTile extends ConstraintLayout {

    private TextView mTextView;
    private ImageView mIcon;

    public ResourceLinkTile(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.resource_link_tile, this, true);
        findViews();

        // Connect custom attributes with the view
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ResourceLinkTile,
                0, 0);

        try {
            final String title = a.getString(R.styleable.ResourceLinkTile_title);
            final String url = a.getString(R.styleable.ResourceLinkTile_url);
            final int iconResourceId = a.getResourceId(R.styleable.ResourceLinkTile_icon, R.drawable.link_placeholder);

            mTextView.setText(title);
            mIcon.setImageResource(iconResourceId);
            mIcon.setContentDescription(title + " icon.");
            // TODO: make sure the buttons/links work on screen reader
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Open an activity instead of an url (for wizard activity)
                    openBroswer(url);
                }
            });
        } finally {
            a.recycle();
        }
    }

    private void openBroswer(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(url));
        getContext().startActivity(intent);
    }

    private void findViews() {
        // Locate the views
        mTextView = findViewById(R.id.resource_link_name_tv);
        mIcon = findViewById(R.id.resource_link_iv);
    }
}
