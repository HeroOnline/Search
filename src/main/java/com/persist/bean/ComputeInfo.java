package com.persist.bean;

import java.io.Serializable;

/**
 * Created by taozhiheng on 16-10-21.
 *
 * base information to compute feature value of face image
 *
 */
public class ComputeInfo implements Serializable{

    public String key;
    public byte[] value;
    public int rows;
    public int columns;

    public ComputeInfo(String key, byte[] value, int rows, int columns)
    {
        this.key = key;
        this.value = value;
        this.rows = rows;
        this.columns = columns;
    }

}
