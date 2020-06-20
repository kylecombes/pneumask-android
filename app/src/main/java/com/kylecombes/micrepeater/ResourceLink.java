package com.kylecombes.micrepeater;

import android.net.Uri;

public class ResourceLink {
    private int mTextResId;
    private Integer mImageResId;
    private Uri mLinkUrl;

    public ResourceLink(int textResId, Integer imageResId, Uri linkUrl) {
        mTextResId = textResId;
        mImageResId = imageResId;
        mLinkUrl = linkUrl;
    }

    public int getTextResId() {
        return mTextResId;
    }

    public Integer getImageResId() {
        return mImageResId;
    }

    public Uri getLinkUrl() {
        return mLinkUrl;
    }
}