package cn.entertech.biomoduledemo;

import android.util.Log;

public class ConvertUtil {
    public static int converUnchart(byte data) {
        Log.d("###","unchar is "+(data & 0xff));
        return (data & 0xff);
    }
}
