package cn.entertech.biomoduledemo.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import static cn.entertech.biomoduledemo.app.Constant.SP_APP_KEY;
import static cn.entertech.biomoduledemo.app.Constant.SP_APP_SECRET;
import static cn.entertech.biomoduledemo.app.Constant.SP_SETTING;
import static cn.entertech.biomoduledemo.app.Constant.SP_USER_ID;


/**
 * Created by EnterTech on 2017/1/10.
 */

public class SettingManager {
    private static SettingManager mInstance;
    private android.app.Application mApplication;

    public static SettingManager getInstance() {
        if (null == mInstance) {
            synchronized (SettingManager.class) {
                if (null == mInstance) {
                    mInstance = new SettingManager(cn.entertech.biomoduledemo.app.Application.Companion.getInstance());
                }
            }
        }
        return mInstance;
    }

    private SettingManager(Application application) {
        mApplication = application;
    }

    private SharedPreferences getSharedPreferences() {
        return mApplication.getSharedPreferences(SP_SETTING, Context.MODE_PRIVATE);
    }

    public void saveCredentials(String appKey, String appSecret, String userId) {
        getSharedPreferences().edit()
                .putString(SP_APP_KEY, appKey)
                .putString(SP_APP_SECRET, appSecret)
                .putString(SP_USER_ID, userId)
                .apply();
    }

    public String getAppKey() {
        return getSharedPreferences().getString(SP_APP_KEY, "");
    }

    public String getAppSecret() {
        return getSharedPreferences().getString(SP_APP_SECRET, "");
    }

    public String getUserId() {
        return getSharedPreferences().getString(SP_USER_ID, "");
    }
}
