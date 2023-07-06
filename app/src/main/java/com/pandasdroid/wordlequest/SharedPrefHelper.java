package com.pandasdroid.wordlequest;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefHelper {
    public int coins;
    public int highScore;

    SharedPreferences sharedPreferences;

    public SharedPrefHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(GameConstants.Pref_Name, Context.MODE_PRIVATE);
    }

    public SharedPreferences getSharedPrefHelper() {
        return sharedPreferences;
    }

    public SharedPreferences.Editor edit() {
        return sharedPreferences.edit();
    }

    public int getIntValue(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public int putCoins(int coins) {
        int prevCoins = getIntValue("coins");
        edit().putInt("coins", prevCoins + coins).apply();
        return getIntValue("coins");
    }
}