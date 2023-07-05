package com.pandasdroid.wordlequest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.pandasdroid.wordlequest.databinding.ActivityLendingBinding;
import com.pandasdroid.wordlequest.databinding.DialogSettingsBinding;
import com.pandasdroid.wordlequest.databinding.DialogWordOfTheDayBinding;
import com.pandasdroid.wordlequest.room.AppDatabase;
import com.pandasdroid.wordlequest.room.HistoryDao;
import com.pandasdroid.wordlequest.room.HistoryEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LendingActivity extends AppCompatActivity {

    private ActivityLendingBinding binding;
    private List<HistoryEntity> historyList = new ArrayList<>();
    private HistoryAdapter historyAdapter;

    private RewardedAd rewardedAd;
    private boolean earnedReward = false;
    private android.app.AlertDialog daily_words_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLendingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Load rewarded ad
        loadRewardedAd();


        //Fetching History Data
        AppDatabase database = AppDatabase.getInstance(LendingActivity.this);
        HistoryDao historyDao = database.userDao();
        historyList = historyDao.getHistory();

        binding.btnWordOfTheDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogWordOfTheDayBinding wotBinding = DialogWordOfTheDayBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
                AlertDialog.Builder dialog = new AlertDialog.Builder(LendingActivity.this);
                // Set up RecyclerView with GridLayoutManager
                GridLayoutManager layoutManager = new GridLayoutManager(LendingActivity.this, 7);
                wotBinding.rvDailyWords.setLayoutManager(layoutManager);

                final int[] counter = {0};

                // Set up CalendarAdapter and attach it to RecyclerView
                CalendarAdapter calendarAdapter = new CalendarAdapter(getCalendarData(new GetMonthYear(counter).getMonth() - 1, new GetMonthYear(counter).getYear()), new GetMonthYear(counter).getMonth() - 1, new GetMonthYear(counter).getYear(), new MonthAdapterListener() {
                    @Override
                    public void onSuccessGridSize(int size) {
                        //Build Header
                        LinearLayout weekdaysLayout = wotBinding.llHeader;

                        // Define the week days
                        String[] weekDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

                        for (String day : weekDays) {
                            TextView textView = new TextView(LendingActivity.this);
                            textView.setText(day);
                            textView.setTextColor(getResources().getColor(R.color.black));
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                            textView.setTypeface(Typeface.create("gothambold", Typeface.BOLD));
                            textView.setGravity(Gravity.CENTER);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
                            textView.setLayoutParams(layoutParams);
                            weekdaysLayout.addView(textView);
                        }
                    }

                    @Override
                    public void onSelectDate(int year, int month, int day) {
                        // Get the date in the format "2023-01-01"
                        // Set the date in the Calendar object
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month - 1, day);

                        // Get the date in the format "yyyy-MM-dd"
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String formattedDate = dateFormat.format(calendar.getTime());
                        String wordData = new WordOfTheDayFetcher(LendingActivity.this).fetchWordByDate(formattedDate);
                        // Get the date in the format "yyyy-MM-dd"
                        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(LendingActivity.this);
                        if (!sharedPrefHelper.getSharedPrefHelper().contains(formattedDate)) {
                            wotBinding.ivAds.setVisibility(View.VISIBLE);
                            wotBinding.btnStart.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (rewardedAd != null) {
                                        rewardedAd.show(LendingActivity.this, new OnUserEarnedRewardListener() {
                                            @Override
                                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                                earnedReward = true;
                                            }
                                        });
                                        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                            @Override
                                            public void onAdDismissedFullScreenContent() {
                                                if (earnedReward) {
                                                    earnedReward = false;
                                                    // User earned the reward, handle the reward completion here
                                                    startNextActivity(wordData, formattedDate);
                                                    loadRewardedAd();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            wotBinding.ivAds.setVisibility(View.GONE);
                            wotBinding.btnStart.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startNextActivity(wordData, formattedDate);
                                }
                            });
                        }
                    }
                });

                wotBinding.rvDailyWords.setAdapter(calendarAdapter);

                wotBinding.monthTextView.setText(new GetMonthYear(counter).getMonthName() + " " + new GetMonthYear(counter).getYear());
                dialog.setView(wotBinding.getRoot());
                AlertDialog alert = dialog.create();


                wotBinding.btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();
                    }
                });

                wotBinding.prevMonth.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (new GetMonthYear(counter).getMonth() == 1 && new GetMonthYear(counter).getYear() == 2023) {
                            return;
                        }
                        counter[0]--;
                        wotBinding.monthTextView.setText(new GetMonthYear(counter).getMonthName() + " " + new GetMonthYear(counter).getYear());
                        // Set up CalendarAdapter and attach it to RecyclerView
                        CalendarAdapter calendarAdapter = new CalendarAdapter(getCalendarData(new GetMonthYear(counter).getMonth() - 1, new GetMonthYear(counter).getYear()), new GetMonthYear(counter).getMonth() - 1, new GetMonthYear(counter).getYear(), new MonthAdapterListener() {
                            @Override
                            public void onSuccessGridSize(int size) {
                            }

                            @Override
                            public void onSelectDate(int year, int month, int day) {
                                // Get the date in the format "2023-01-01"
                                // Set the date in the Calendar object
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year, month - 1, day);

                                // Get the date in the format "yyyy-MM-dd"
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                String formattedDate = dateFormat.format(calendar.getTime());
                                String wordData = new WordOfTheDayFetcher(LendingActivity.this).fetchWordByDate(formattedDate);
                                // Get the date in the format "yyyy-MM-dd"
                                SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(LendingActivity.this);
                                if (!sharedPrefHelper.getSharedPrefHelper().contains(formattedDate)) {
                                    wotBinding.ivAds.setVisibility(View.VISIBLE);
                                    wotBinding.btnStart.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (rewardedAd != null) {
                                                rewardedAd.show(LendingActivity.this, new OnUserEarnedRewardListener() {
                                                    @Override
                                                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                                        earnedReward = true;
                                                    }
                                                });
                                                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                                    @Override
                                                    public void onAdDismissedFullScreenContent() {
                                                        if (earnedReward) {
                                                            earnedReward = false;
                                                            // User earned the reward, handle the reward completion here
                                                            startNextActivity(wordData, formattedDate);
                                                            loadRewardedAd();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                } else {
                                    wotBinding.ivAds.setVisibility(View.GONE);
                                    wotBinding.btnStart.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startNextActivity(wordData, formattedDate);
                                        }
                                    });
                                }
                            }

                        });
                        wotBinding.rvDailyWords.setAdapter(calendarAdapter);
                    }
                });

                wotBinding.nextMonth.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (new GetMonthYear(counter).getMonth() == 12 && new GetMonthYear(counter).getYear() == 2030) {
                            return;
                        }
                        if (counter[0] < 0) {
                            counter[0]++;
                            wotBinding.monthTextView.setText(new GetMonthYear(counter).getMonthName() + " " + new GetMonthYear(counter).getYear());
                            // Set up CalendarAdapter and attach it to RecyclerView
                            CalendarAdapter calendarAdapter = new CalendarAdapter(getCalendarData(new GetMonthYear(counter).getMonth() - 1, new GetMonthYear(counter).getYear()), new GetMonthYear(counter).getMonth() - 1, new GetMonthYear(counter).getYear(), new MonthAdapterListener() {
                                @Override
                                public void onSuccessGridSize(int size) {
                                }

                                @Override
                                public void onSelectDate(int year, int month, int day) {
                                    // Get the date in the format "2023-01-01"
                                    // Set the date in the Calendar object
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.set(year, month - 1, day);

                                    // Get the date in the format "yyyy-MM-dd"
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                    String formattedDate = dateFormat.format(calendar.getTime());
                                    String wordData = new WordOfTheDayFetcher(LendingActivity.this).fetchWordByDate(formattedDate);

                                    // Get the date in the format "yyyy-MM-dd"
                                    SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(LendingActivity.this);
                                    if (!sharedPrefHelper.getSharedPrefHelper().contains(formattedDate)) {
                                        wotBinding.ivAds.setVisibility(View.VISIBLE);
                                        wotBinding.btnStart.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (rewardedAd != null) {
                                                    rewardedAd.show(LendingActivity.this, new OnUserEarnedRewardListener() {
                                                        @Override
                                                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                                            earnedReward = true;
                                                        }
                                                    });
                                                    rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                                        @Override
                                                        public void onAdDismissedFullScreenContent() {
                                                            if (earnedReward) {
                                                                earnedReward = false;
                                                                startNextActivity(wordData, formattedDate);
                                                                loadRewardedAd();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    } else {
                                        wotBinding.ivAds.setVisibility(View.GONE);
                                        wotBinding.btnStart.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                startNextActivity(wordData, formattedDate);
                                            }
                                        });
                                    }
                                }
                            });
                            wotBinding.rvDailyWords.setAdapter(calendarAdapter);
                        }
                    }
                });
                Window dWin = alert.getWindow();
                if (dWin != null) {
                    dWin.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    dWin.setWindowAnimations(R.style.DialogAnimation);
                    dWin.setBackgroundDrawable(null);
                }
                alert.show();
            }
        });
        binding.ivStatics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StatisticsBuilder.buildStatistics(historyList, LendingActivity.this);
            }
        });
        binding.settingsDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(LendingActivity.this);
                DialogSettingsBinding dialogSettingsBinding = DialogSettingsBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
                builder.setView(dialogSettingsBinding.getRoot());
                daily_words_dialog = builder.create();

                initializeCardSelection(dialogSettingsBinding);

                SharedPreferences sharedPreferences = getSharedPreferences("App", Context.MODE_PRIVATE);
                String selectedValue = sharedPreferences.getString("selectedValue", "5");
                if (!selectedValue.isEmpty()) {
                    // Find the corresponding card view based on the selected value
                    CardView selectedCardView = null;

                    if (selectedValue.equals("4")) {
                        selectedCardView = dialogSettingsBinding.cardView1;
                    } else if (selectedValue.equals("5")) {
                        selectedCardView = dialogSettingsBinding.cardView2;
                    } else if (selectedValue.equals("6")) {
                        selectedCardView = dialogSettingsBinding.cardView3;
                    } else if (selectedValue.equals("7")) {
                        selectedCardView = dialogSettingsBinding.cardView4;
                    } else if (selectedValue.equals("8")) {
                        selectedCardView = dialogSettingsBinding.cardView5;
                    } else if (selectedValue.equals("9")) {
                        selectedCardView = dialogSettingsBinding.cardView6;
                    } else if (selectedValue.equals("10")) {
                        selectedCardView = dialogSettingsBinding.cardView7;
                    }

                    // Perform the click action on the selected card view
                    if (selectedCardView != null) {
                        selectedCardView.performClick();
                    }
                }


                daily_words_dialog.show();
                Window dWin = daily_words_dialog.getWindow();
                if (dWin != null) {
                    dWin.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    dWin.setWindowAnimations(R.style.DialogAnimation);
                    dWin.setBackgroundDrawable(null);
                }
            }
        });
    }

    private CardView selectedCard;


    private RewardedVideoAd rewardedVideoAd;

    void loadFacebookRewardedVideoAd() {
        rewardedVideoAd = new RewardedVideoAd(this, "YOUR_PLACEMENT_ID");
        RewardedVideoAdListener rewardedVideoAdListener = new RewardedVideoAdListener() {
            @Override
            public void onError(Ad ad, AdError error) {
            }

            @Override
            public void onAdLoaded(Ad ad) {
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }

            @Override
            public void onRewardedVideoCompleted() {
                rewardedVideoAd = null;
                if (AudienceNetworkAds.isInitialized(getApplicationContext())) {
                    loadFacebookRewardedVideoAd();
                }
            }

            @Override
            public void onRewardedVideoClosed() {
            }
        };
        rewardedVideoAd.loadAd(rewardedVideoAd.buildLoadAdConfig().withAdListener(rewardedVideoAdListener).build());
    }


    private void loadRewardedAd() {
        // Replace with your own ad unit ID
        String adUnitId = "ca-app-pub-3940256099942544/5224354917";

        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, adUnitId, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                LendingActivity.this.rewardedAd = rewardedAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                rewardedAd = null;
            }
        });
    }

    private void startNextActivity(String wordData, String formattedDate) {
        // Save the word in SharedPreferences
        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(LendingActivity.this);
        sharedPrefHelper.edit().putString(formattedDate, wordData).apply();
        Intent intent = new Intent(LendingActivity.this, MainActivity.class);
        intent.putExtra("correct", wordData);
        if (daily_words_dialog != null && daily_words_dialog.isShowing()) {
            daily_words_dialog.dismiss();
        }
        startActivity(intent);
    }

    private void initializeCardSelection(DialogSettingsBinding binding) {

        // Set click listeners for all card views
        binding.cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCardSelection(binding.cardView1);
            }
        });
        binding.cardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCardSelection(binding.cardView2);
            }
        });
        binding.cardView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCardSelection(binding.cardView3);
            }
        });
        binding.cardView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCardSelection(binding.cardView4);
            }
        });
        binding.cardView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCardSelection(binding.cardView5);
            }
        });
        binding.cardView6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCardSelection(binding.cardView6);
            }
        });
        binding.cardView7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCardSelection(binding.cardView7);
            }
        });
    }

    private void handleCardSelection(CardView cardView) {
        // Reset background color of previously selected card (if any)
        if (selectedCard != null) {
            selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
        }

        // Set background color of the newly selected card
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.selected_green));

        // Update the selected card reference
        selectedCard = cardView;

        // Get the text value from the selected card
        String selectedValue = ((TextView) cardView.getChildAt(0)).getText().toString();

        // Save the selected value in SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("App", Context.MODE_PRIVATE).edit();
        editor.putString("selectedValue", selectedValue);
        editor.apply();
    }


    private List<Integer> getCalendarData(int month, int year) {
        List<Integer> calendarData = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);

        int maximumDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Add empty cells for the days before the start of the month
        for (int i = 0; i < 35 - maximumDayOfMonth; i++) {
            calendarData.add(null);
        }

        // Add the day numbers for each cell in the calendar
        for (int i = 1; i <= maximumDayOfMonth; i++) {
            calendarData.add(i);
        }

        return calendarData;
    }

}