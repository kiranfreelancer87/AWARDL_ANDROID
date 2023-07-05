package com.pandasdroid.wordlequest;

import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.util.Calendar;

public class GetMonthYear {
    private int year;
    private int month;
    private int[] counter;

    public GetMonthYear(int[] counter) {
        this.counter = counter;
        Calendar calendar = Calendar.getInstance();
        setYear(calendar.get(Calendar.YEAR));
        setMonth(calendar.get(Calendar.MONTH));
        updateMonth();
    }

    public void updateMonth() {
        month += counter[0];
        if (counter[0] > 0) {
            while (month > 12) {
                month -= 12;
                year++;
            }
        } else if (counter[0] < 0) {
            while (month < 1) {
                month += 12;
                year--;
            }
        }
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public String getMonthName() {
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        return months[month - 1];
    }

    public void setMonth(int month) {
        this.month = month;
    }

}
