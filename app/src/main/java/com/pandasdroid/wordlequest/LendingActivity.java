package com.pandasdroid.wordlequest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.pandasdroid.wordlequest.databinding.ActivityLendingBinding;
import com.pandasdroid.wordlequest.databinding.DialogSettingsBinding;
import com.pandasdroid.wordlequest.databinding.DialogWordOfTheDayBinding;
import com.pandasdroid.wordlequest.databinding.HintDialogBinding;
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
    private AlertDialog daily_words_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLendingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHowToPlayDialog();
            }
        });

        /*AdLoader adLoader = new AdLoader.Builder(this, GameConstants.NativeAdId)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        NativeTemplateStyle styles = new NativeTemplateStyle.Builder().build();
                        binding.myTemplate.setVisibility(View.VISIBLE);
                        TemplateView template = binding.myTemplate;
                        template.setStyles(styles);
                        template.setNativeAd(nativeAd);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        // Handle the failure by logging, altering the UI, and so on.
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());*/

        // Load rewarded ad
        loadRewardedAd();


        //Fetching History Data
        AppDatabase database = AppDatabase.getInstance(LendingActivity.this);
        HistoryDao historyDao = database.userDao();
        historyList = historyDao.getHistory();

        binding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LendingActivity.this, MainActivity.class));
            }
        });

        binding.btnWordOfTheDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogWordOfTheDayBinding wotBinding = DialogWordOfTheDayBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
                AlertDialog.Builder dialog = new AlertDialog.Builder(LendingActivity.this);

                // Set up RecyclerView with GridLayoutManager
                GridLayoutManager layoutManager = new NoScrollLayoutManager(LendingActivity.this, 7, RecyclerView.VERTICAL, false);
                wotBinding.rvDailyWords.setLayoutManager(layoutManager);

                //Set Headers
                wotBinding.rvHeaderLayout.setLayoutManager(new NoScrollLayoutManager(LendingActivity.this, 7, LinearLayoutManager.VERTICAL, false));
                wotBinding.rvHeaderLayout.setAdapter(new CalendarHeaderAdapter());


                final int[] counter = {0};

                // Set up CalendarAdapter and attach it to RecyclerView
                CalendarAdapter calendarAdapter = new CalendarAdapter(new GetMonthYear(counter).getMonthDays(), new GetMonthYear(counter).getMonth(), new GetMonthYear(counter).getYear(), new MonthAdapterListener() {
                    @Override
                    public void onSuccessGridSize(int size) {

                    }

                    @Override
                    public void onSelectDate(int year, int month, int day) {
                        // Get the date in the format "2023-01-01"
                        // Set the date in the Calendar object
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month, day);

                        // Get the date in the format "yyyy-MM-dd"
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String formattedDate = dateFormat.format(calendar.getTime());
                        String wordData = new WordOfTheDayFetcher(LendingActivity.this).fetchWordByDate(formattedDate);
                        // Get the date in the format "yyyy-MM-dd"
                        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(LendingActivity.this);
                        if (!sharedPrefHelper.getSharedPrefHelper().contains(formattedDate)) {
                            wotBinding.ivAds.setImageDrawable(ContextCompat.getDrawable(LendingActivity.this, R.drawable.advertising));
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
                            wotBinding.ivAds.setImageDrawable(null);
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
                wotBinding.rvDailyWords.getAdapter().notifyDataSetChanged();

                wotBinding.monthTextView.setText(new GetMonthYear(counter).getMonthNameAndYear());
                dialog.setView(wotBinding.getRoot());
                daily_words_dialog = dialog.create();


                wotBinding.btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        daily_words_dialog.dismiss();
                    }
                });

                wotBinding.prevMonth.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (new GetMonthYear(counter).getMonth() == 1 && new GetMonthYear(counter).getYear() == 2023) {
                            return;
                        }
                        counter[0]--;
                        wotBinding.monthTextView.setText(new GetMonthYear(counter).getMonthNameAndYear());
                        // Set up CalendarAdapter and attach it to RecyclerView
                        CalendarAdapter calendarAdapter = new CalendarAdapter(new GetMonthYear(counter).getMonthDays(), new GetMonthYear(counter).getMonth(), new GetMonthYear(counter).getYear(), new MonthAdapterListener() {
                            @Override
                            public void onSuccessGridSize(int size) {
                            }

                            @Override
                            public void onSelectDate(int year, int month, int day) {
                                // Get the date in the format "2023-01-01"
                                // Set the date in the Calendar object
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year, month, day);

                                // Get the date in the format "yyyy-MM-dd"
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                String formattedDate = dateFormat.format(calendar.getTime());
                                String wordData = new WordOfTheDayFetcher(LendingActivity.this).fetchWordByDate(formattedDate);
                                // Get the date in the format "yyyy-MM-dd"
                                SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(LendingActivity.this);
                                if (!sharedPrefHelper.getSharedPrefHelper().contains(formattedDate)) {
                                    wotBinding.ivAds.setImageDrawable(ContextCompat.getDrawable(LendingActivity.this, R.drawable.advertising));

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
                                    wotBinding.ivAds.setImageDrawable(null);
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
                            wotBinding.monthTextView.setText(new GetMonthYear(counter).getMonthNameAndYear());
                            // Set up CalendarAdapter and attach it to RecyclerView
                            CalendarAdapter calendarAdapter = new CalendarAdapter(new GetMonthYear(counter).getMonthDays(), new GetMonthYear(counter).getMonth(), new GetMonthYear(counter).getYear(), new MonthAdapterListener() {
                                @Override
                                public void onSuccessGridSize(int size) {
                                }

                                @Override
                                public void onSelectDate(int year, int month, int day) {
                                    // Get the date in the format "2023-01-01"
                                    // Set the date in the Calendar object
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.set(year, month, day);

                                    // Get the date in the format "yyyy-MM-dd"
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                    String formattedDate = dateFormat.format(calendar.getTime());
                                    String wordData = new WordOfTheDayFetcher(LendingActivity.this).fetchWordByDate(formattedDate);

                                    // Get the date in the format "yyyy-MM-dd"
                                    SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(LendingActivity.this);
                                    if (!sharedPrefHelper.getSharedPrefHelper().contains(formattedDate)) {
                                        wotBinding.ivAds.setImageDrawable(ContextCompat.getDrawable(LendingActivity.this, R.drawable.advertising));

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
                                        wotBinding.ivAds.setImageDrawable(null);
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
                Window dWin = daily_words_dialog.getWindow();
                if (dWin != null) {
                    dWin.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    dWin.setWindowAnimations(R.style.DialogAnimation);
                    dWin.setBackgroundDrawable(null);
                }
                daily_words_dialog.show();
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
                android.app.AlertDialog dialog = builder.create();

                initializeCardSelection(dialogSettingsBinding);

                SharedPreferences sharedPreferences = getSharedPreferences(GameConstants.Pref_Name, Context.MODE_PRIVATE);
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


                dialog.show();
                Window dWin = dialog.getWindow();
                if (dWin != null) {
                    dWin.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    dWin.setWindowAnimations(R.style.DialogAnimation);
                    dWin.setBackgroundDrawable(null);
                }
            }
        });
    }

    private void showHowToPlayDialog() {
        HintDialogBinding hintDialogBinding = HintDialogBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
        androidx.appcompat.app.AlertDialog.Builder dialog_builder = new androidx.appcompat.app.AlertDialog.Builder(LendingActivity.this);
        androidx.appcompat.app.AlertDialog dialog = dialog_builder.create();

        dialog.setView(hintDialogBinding.getRoot());

        dialog.setCancelable(false);

        hintDialogBinding.btnClose.setOnClickListener((v) -> {
            dialog.dismiss();
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });

        //Show Dialog on Success Loading Words
        dialog.show();
        Window dWin = dialog.getWindow();
        if (dWin != null) {
            dWin.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            dWin.setWindowAnimations(R.style.DialogAnimation);
            dWin.setBackgroundDrawable(null);
        }
    }


    private CardView selectedCard;


    private RewardedVideoAd rewardedVideoAd;

    void loadFacebookRewardedVideoAd() {
        rewardedVideoAd = new RewardedVideoAd(this, GameConstants.FacebookRewardedAdsID);
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

        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, GameConstants.RewardedVideoAdsID, adRequest, new RewardedAdLoadCallback() {
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
        SharedPreferences.Editor editor = getSharedPreferences(GameConstants.Pref_Name, Context.MODE_PRIVATE).edit();
        editor.putString("selectedValue", selectedValue);
        editor.apply();
    }
}