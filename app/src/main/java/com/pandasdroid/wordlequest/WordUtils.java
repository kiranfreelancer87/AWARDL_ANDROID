package com.pandasdroid.wordlequest;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class WordUtils {
    private static final String WORDS_FILENAME = "words.json";
    private static final Random RANDOM = new Random();
    private static final String PREFS_NAME = "UsedWordsPrefs";
    private static final String USED_WORDS_KEY = "used_words";

    private static Set<Word> loadWords(Context context) {
        Set<Word> words = new HashSet<>();
        try {
            InputStream inputStream = context.getAssets().open(WORDS_FILENAME);
            InputStreamReader reader = new InputStreamReader(inputStream);
            Word[] wordArray = new Gson().fromJson(reader, Word[].class);
            if (wordArray != null) {
                words = new HashSet<>(Arrays.asList(wordArray));
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }

    public static String getWordDefinition(Context context, String word) {
        Set<Word> words = loadWords(context);
        for (Word w : words) {
            if (w.getWord().equalsIgnoreCase(word)) {
                return w.getMeaning();
            }
        }
        return null;
    }


    public static String getRandomWord(Context context, int wordLength) {
        Set<Word> words = loadWords(context);
        List<Word> filteredWords = new ArrayList<>();
        for (Word word : words) {
            if (word.getWord().length() == wordLength) {
                filteredWords.add(word);
            }
        }
        if (!filteredWords.isEmpty()) {
            while (!filteredWords.isEmpty()) {
                int randomIndex = RANDOM.nextInt(filteredWords.size());
                Word randomWord = filteredWords.get(randomIndex);
                if (!isWordUsed(context, randomWord.getWord())) {
                    saveUsedWord(context, randomWord.getWord());
                    return randomWord.getWord();
                }
                filteredWords.remove(randomIndex);
            }
        }
        return getRandomUnusedWord(context);
    }

    private static String getRandomUnusedWord(Context context) {
        Set<Word> words = loadWords(context);
        List<Word> unusedWords = new ArrayList<>();
        for (Word word : words) {
            if (!isWordUsed(context, word.getWord())) {
                unusedWords.add(word);
            }
        }
        if (!unusedWords.isEmpty()) {
            int randomIndex = RANDOM.nextInt(unusedWords.size());
            Word randomWord = unusedWords.get(randomIndex);
            saveUsedWord(context, randomWord.getWord());
            return randomWord.getWord();
        }
        return null;
    }


    private static void saveUsedWord(Context context, String word) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> usedWordsSet = sharedPreferences.getStringSet(USED_WORDS_KEY, new HashSet<>());
        usedWordsSet.add(word);
        sharedPreferences.edit().putStringSet(USED_WORDS_KEY, usedWordsSet).apply();
    }

    public static boolean isWordUsed(Context context, String word) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> usedWordsSet = sharedPreferences.getStringSet(USED_WORDS_KEY, new HashSet<>());
        return usedWordsSet.contains(word);
    }
}
