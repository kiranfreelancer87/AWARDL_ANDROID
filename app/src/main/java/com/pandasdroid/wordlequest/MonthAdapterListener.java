package com.pandasdroid.wordlequest;

public interface MonthAdapterListener {
    default void onSuccessGridSize(int size){

    }

    default void onSelectDate(int year, int month, int day){};
}
