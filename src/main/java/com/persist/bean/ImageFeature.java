package com.persist.bean;

import java.io.Serializable;

/**
 * Created by taozhiheng on 16-10-21.
 *
 */
public class ImageFeature implements Serializable{

    public ImageInfo info;

    public float[] feature;

    public ImageFeature()
    {

    }

    public ImageFeature(ImageInfo info, float[] feature)
    {
        this.info = info;
        this.feature = feature;
    }
}
