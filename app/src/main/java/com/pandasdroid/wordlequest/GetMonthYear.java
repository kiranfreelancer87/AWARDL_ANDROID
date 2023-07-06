package com.pandasdroid.wordlequest;

import android.util.Log;

import com.google.gson.Gson;

import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GetMonthYear {
    private int year;
    private int month;
    private int[] counter;
    private final Calendar cal;

    public GetMonthYear(int[] counter) {
        this.counter = counter;
        Calendar calendar = Calendar.getInstance();
        setYear(calendar.get(Calendar.YEAR));
        setMonth(calendar.get(Calendar.MONTH));
        updateMonth();
        Log.wtf("Year-Month-Counter", calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-" + counter[0]);


        cal = Calendar.getInstance();
        // Update the cal variable with the new year and month values
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month); // Subtract 1 as Calendar months are zero-based
        // Create a SimpleDateFormat with the desired date format pattern
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        // Format the month and get the month name
        String name = dateFormat.format(cal.getTime()) + " " + year;
        Log.wtf("MonthName", name);
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

    public String getMonthNameAndYear() {
        // Create a SimpleDateFormat with the desired date format pattern
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        // Format the month and get the month name
        String name = dateFormat.format(cal.getTime()) + " " + year;
        Log.wtf("MonthName", name);
        return name;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public List<Integer> getMonthDays() {
        // Create a Calendar instance with the current year and month
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month); // Subtract 1 as Calendar months are zero-based

        // Get the number of days in the month
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Get the first day of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Calculate the number of empty days to add at the beginning
        int numEmptyDays = (firstDayOfWeek + 5) % 7; // Adjust for zero-based indexing

        // Create a list to store the days
        List<Integer> monthDays = new ArrayList<>();

        // Add empty placeholders at the beginning
        for (int i = 0; i < numEmptyDays; i++) {
            monthDays.add(null);
        }

        // Add the days to the list
        for (int day = 1; day <= daysInMonth; day++) {
            monthDays.add(day);
        }

        return monthDays;
    }

    public List<String> getMonthDayNames() {
        // Create a Calendar instance with the current year and month
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month); // Subtract 1 as Calendar months are zero-based

        // Get the number of days in the month
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Create a list to store the days
        List<String> monthDays = new ArrayList<>();

        // Get the date format pattern for day of the week (e.g., "EEE" for abbreviated name)
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d-MMMM-yyyy", Locale.getDefault());

        // Add the day of the week names and month days to the list
        for (int day = 1; day <= daysInMonth; day++) {
            calendar.set(Calendar.DAY_OF_MONTH, day);
            String dayOfWeekName = dateFormat.format(calendar.getTime());
            monthDays.add(dayOfWeekName + " " + day);
        }

        // Add empty placeholders for days outside the current month
        while (monthDays.size() <= 35) {
            monthDays.add(0, "");
        }

        return monthDays;
    }


}
