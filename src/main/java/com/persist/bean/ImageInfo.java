package com.persist.bean;

import java.io.Serializable;

/**
 * Created by taozhiheng on 16-10-20.
 *
 * face image base information:
 *
 *
 */
public class ImageInfo implements Serializable {

    //the normal face image
    public final static int TYPE_NORMAL = 0;
    //the face image which is used to search similar image records
    public final static int TYPE_SAMPLE = 1;

    //the hdfs ulr or http url of the face image
    public String url;
    //the identity of the face image, maybe it is the camera name or others
    public String video_id;
    //the timestamp when the face image is grabbed
    public String time_stamp;
    //whether the face image is store in the inner hdfs
    public boolean inner = true;
    //the type of the face image
    public int type = TYPE_NORMAL;

    public ImageInfo() {

    }

    public ImageInfo(String url, String video_id, String time_stamp, boolean inner, int type) {
        this.url = url;
        this.video_id = video_id;
        this.time_stamp = time_stamp;
        this.inner = inner;
        this.type = type;
    }
}
