package com.pandasdroid.wordlequest;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WordOfTheDayFetcher {
    private final Context context;

    public WordOfTheDayFetcher(Context context) {
        this.context = context;
    }

    public String fetchWordByDate(String targetDate) {
        String word = null;

        try {
            JSONArray jsonArray = loadJSONFromAsset();
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String date = jsonObject.optString("date");
                    if (date.equals(targetDate)) {
                        word = jsonObject.optString("word");
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return word;
    }

    public String fetchMeaningByDate(String targetDate) {
        String meaning = null;

        try {
            JSONArray jsonArray = loadJSONFromAsset();
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String date = jsonObject.optString("date");
                    if (date.equals(targetDate)) {
                        meaning = jsonObject.optString("meaning");
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return meaning;
    }

    private JSONArray loadJSONFromAsset() {
        JSONArray jsonArray = null;
        String json;
        try {
            InputStream inputStream = context.getAssets().open("wordoftheday.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, StandardCharsets.UTF_8);
            jsonArray = new JSONArray(json);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return jsonArray;
    }
}
