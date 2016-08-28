package org.kowems.android.camera;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Eric Ju on 2016/8/28.
 */
public class MyTime {
    public String getYMDHMS() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());


        return formatter.format(curDate);
    }
}
