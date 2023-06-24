package com.piddlepops.awardl;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.os.Bundle;

import com.piddlepops.awardl.databinding.ActivityLendingBinding;
import com.piddlepops.awardl.room.AppDatabase;
import com.piddlepops.awardl.room.HistoryDao;
import com.piddlepops.awardl.room.HistoryEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LendingActivity extends AppCompatActivity {

    private ActivityLendingBinding binding;
    private List<HistoryEntity> historyList = new ArrayList<>();
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLendingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnStart.setOnClickListener((v) -> {
            startActivity(new Intent(this, MainActivity.class));
        });

        AppDatabase database = AppDatabase.getInstance(LendingActivity.this);
        HistoryDao historyDao = database.userDao();
        historyList = historyDao.getHistory();

        // Set up RecyclerView with GridLayoutManager
        GridLayoutManager layoutManager = new GridLayoutManager(this, 7);
        binding.rvHistory.setLayoutManager(layoutManager);

        // Set up CalendarAdapter and attach it to RecyclerView
        CalendarAdapter calendarAdapter = new CalendarAdapter(getCalendarData());
        binding.rvHistory.setAdapter(calendarAdapter);

        // Update month header
        Calendar calendar = Calendar.getInstance();
        String monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        int year = calendar.get(Calendar.YEAR);
        binding.monthTextView.setText(monthName + " " + year);
    }

    private List<Integer> getCalendarData() {
        List<Integer> calendarData = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int maximumDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Add empty cells for the days before the start of the month
        for (int i = 1; i < startDayOfWeek; i++) {
            calendarData.add(null);
        }

        // Add the day numbers for each cell in the calendar
        for (int i = 1; i <= maximumDayOfMonth; i++) {
            calendarData.add(i);
        }

        return calendarData;
    }
}