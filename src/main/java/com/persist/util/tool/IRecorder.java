package com.persist.util.tool;

import java.io.Serializable;

/**
 * Created by taozhiheng on 16-10-21.
 *
 * record some data into hbase
 *
 */
public interface IRecorder<T> extends Serializable {

    void prepare();

    boolean record(T data);

    void cleanup();

}
