package jo.beacon.project.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Map;

public class PreferenceUtil {

    public enum PreferenceKey {
        userId, // 유저 PK
    }

    private volatile static PreferenceUtil mPreferenceUtil;
    private final Context mContext;
    private final String mPackageName;

    public static PreferenceUtil getInstance(Context context) {
        if (mPreferenceUtil == null) {
            synchronized (PreferenceManager.class) {
                if (mPreferenceUtil == null) {
                    mPreferenceUtil = new PreferenceUtil(context);
                }
            }
        }
        return mPreferenceUtil;
    }

    private PreferenceUtil(Context context) {
        mContext = context;
        mPackageName = context.getPackageName();
    }

    public void setStringArrayPref(Context context, PreferenceKey key, ArrayList<String> values) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key.toString(), a.toString());
        } else {
            editor.putString(key.toString(), null);
        }
        editor.apply();
    }

    public ArrayList<String> getStringArrayPref(Context context, PreferenceKey key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key.toString(), null);
        ArrayList<String> urls = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    public boolean put(PreferenceKey key, String value) {
        SharedPreferences pref = mContext.getSharedPreferences(mPackageName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key.name(), value);
        return editor.commit();
    }

    public boolean put(String key, String value) {
        SharedPreferences pref = mContext.getSharedPreferences(mPackageName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public boolean put(PreferenceKey key, boolean value) {
        SharedPreferences pref = mContext.getSharedPreferences(mPackageName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key.name(), value);
        return editor.commit();
    }

    public boolean put(PreferenceKey key, int value) {
        SharedPreferences pref = mContext.getSharedPreferences(mPackageName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key.name(), value);
        return editor.commit();
    }

    public boolean put(PreferenceKey key, long value) {
        SharedPreferences pref = mContext.getSharedPreferences(mPackageName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key.name(), value);
        return editor.commit();
    }

    public boolean put(PreferenceKey key, Long value) {
        if (value == null) {
            return false;
        }

        SharedPreferences pref = mContext.getSharedPreferences(mPackageName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key.name(), value);
        return editor.commit();
    }


    public String get(PreferenceKey key, String defaultValue) {
        SharedPreferences pref = mContext.getSharedPreferences(mPackageName, Activity.MODE_PRIVATE);
        return pref.getString(key.name(), defaultValue);
    }

    public String get(String key, String defaultValue) {
        SharedPreferences pref = mContext.getSharedPreferences(mPackageName, Activity.MODE_PRIVATE);
        return pref.getString(key, defaultValue);
    }

    public int get(PreferenceKey key, int defaultValue) {
        SharedPreferences pref = mContext.getSharedPreferences(mPackageName, Activity.MODE_PRIVATE);
        return pref.getInt(key.name(), defaultValue);
    }

    public long getLong(PreferenceKey key, long defaultValue) {
        SharedPreferences pref = mContext.getSharedPreferences(mPackageName, Activity.MODE_PRIVATE);
        return pref.getLong(key.name(), defaultValue);
    }

    public Long getLong(PreferenceKey key) {
        SharedPreferences pref = mContext.getSharedPreferences(mPackageName, Activity.MODE_PRIVATE);
        long value = pref.getLong(key.name(), -1);
        return value > 0 ? value : null;
    }

    public boolean get(PreferenceKey key, boolean defaultValue) {
        SharedPreferences pref = mContext.getSharedPreferences(mPackageName, Activity.MODE_PRIVATE);
        return pref.getBoolean(key.name(), defaultValue);
    }

    public Map<String, ?> getAll() {
        SharedPreferences pref = mContext.getSharedPreferences(mPackageName, Activity.MODE_PRIVATE);
        return pref.getAll();
    }

    public void remove(PreferenceKey key) {
        remove(key.name());
    }

    public void remove(String key) {
        SharedPreferences pref = mContext.getSharedPreferences(mPackageName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(key);
        editor.commit();
    }
}

