package com.persist.bean;

import java.io.Serializable;

/**
 * Created by taozhiheng on 16-10-21.
 *
 */
public class SearchResult implements Serializable {

    public ImageFeature feature;
    public String result;

    public SearchResult(ImageFeature feature, String result)
    {
        this.feature = feature;
        this.result = result;
    }
}
