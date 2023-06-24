package com.piddlepops.awardl.reswords;

import java.util.ArrayList;
import java.util.Arrays;

public class WordsResponse {
    ArrayList<String> data = new ArrayList<>();

    public WordsResponse(){
        ArrayList<String> tmp = new ArrayList<>(Arrays.asList(
                "apple", "table", "chair", "house", "brush", "shark", "beach", "bread", "flute", "grape",
                "smile", "quick", "lucky", "jelly", "piano", "zebra", "virus", "inbox", "truck", "tiger",
                "lemon", "ocean", "stove", "bunny", "jumbo", "daisy", "fairy", "alarm", "camel", "dough",
                "frogs", "goose", "hotel", "ivory", "juice", "kiwis", "leash", "mango", "novel", "puppy",
                "quilt", "rhino", "salsa", "toxic", "union", "vines", "wheat", "yacht"
        ));
    }

    public ArrayList<String> getData() {
        return data;
    }

    public void setData(ArrayList<String> data) {
        this.data = data;
    }
}
