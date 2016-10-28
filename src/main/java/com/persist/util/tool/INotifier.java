package com.persist.util.tool;

import java.io.Serializable;

/**
 * Created by taozhiheng on 16-10-21.
 *
 * notify specific channels some message with redis
 *
 */
public interface INotifier<T> extends Serializable {

    void prepare();

    boolean notify(String[] channels, T message);

    void cleanup();

}
