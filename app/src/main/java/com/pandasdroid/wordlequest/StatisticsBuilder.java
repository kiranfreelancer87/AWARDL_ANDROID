package com.pandasdroid.wordlequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.pandasdroid.wordlequest.databinding.DialogStatisticsBinding;
import com.pandasdroid.wordlequest.room.HistoryEntity;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class StatisticsBuilder {

    public static void buildStatistics(List<HistoryEntity> historyList, Context context) {
        if (historyList.size() == 0) {
            // Handle the case when no games have been played yet
            // Set all values to 0 or display a message
            // For example:
            Activity activity = (Activity) context;
            DialogStatisticsBinding dialogStatisticsBinding = DialogStatisticsBinding.inflate(activity.getLayoutInflater());

            dialogStatisticsBinding.tvPlayed.setText("0");
            dialogStatisticsBinding.tvWinningPercentage.setText("0%");
            dialogStatisticsBinding.tvMaxStreak.setText("0");
            dialogStatisticsBinding.tvCurrentStreak.setText("0");

            // Set count and progress for each chance to 0
            dialogStatisticsBinding.tvCount1.setText("0");
            dialogStatisticsBinding.verticalProgressView1.setProgress(0);

            dialogStatisticsBinding.tvCount2.setText("0");
            dialogStatisticsBinding.verticalProgressView2.setProgress(0);

            dialogStatisticsBinding.tvCount3.setText("0");
            dialogStatisticsBinding.verticalProgressView3.setProgress(0);

            dialogStatisticsBinding.tvCount4.setText("0");
            dialogStatisticsBinding.verticalProgressView4.setProgress(0);

            dialogStatisticsBinding.tvCount5.setText("0");
            dialogStatisticsBinding.verticalProgressView5.setProgress(0);

            dialogStatisticsBinding.tvCount6.setText("0");
            dialogStatisticsBinding.verticalProgressView6.setProgress(0);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(dialogStatisticsBinding.getRoot());
            AlertDialog dialog = builder.create();
            dialogStatisticsBinding.btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
            Window dWin = dialog.getWindow();
            if (dWin != null) {
                dWin.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                dWin.setWindowAnimations(R.style.DialogAnimation);
                dWin.setBackgroundDrawable(null);
            }
            return;
        }
        int totalGamesPlayed = historyList.size();
        int totalWins = 0;
        int currentStreak = 0;
        int maxStreak = 0;
        int[] chanceDistribution = new int[6];

        for (HistoryEntity history : historyList) {
            if (history.win) {
                totalWins++;
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 0;
            }

            int chances = history.attempts - 1;
            if (chances >= 0 && chances < 6) {
                chanceDistribution[chances]++;
            }
        }


        Activity activity = (Activity) context;


        DialogStatisticsBinding dialogStatisticsBinding = DialogStatisticsBinding.inflate(activity.getLayoutInflater());

        dialogStatisticsBinding.tvPlayed.setText("" + totalGamesPlayed);
        double winRate = (double) totalWins / totalGamesPlayed * 100;

        // Round the win rate to two decimal places
        double roundedWinRate = Math.round(winRate * 100.0) / 100.0;

        // Create a DecimalFormat instance with the desired format pattern
        DecimalFormat decimalFormat = new DecimalFormat("00.00");

        // Format the winRate value
        String formattedWinRate = decimalFormat.format(winRate);

        // Set the formatted winRate value to the TextView
        dialogStatisticsBinding.tvWinningPercentage.setText(formattedWinRate + "%");
        dialogStatisticsBinding.tvMaxStreak.setText("" + maxStreak);
        dialogStatisticsBinding.tvCurrentStreak.setText("" + currentStreak);

        for (int i = 0; i < 6; i++) {
            double distributionPercentage = (double) chanceDistribution[i] / totalGamesPlayed * 100;
            System.out.println((i + 1) + " chance: " + distributionPercentage + "%");

            TextView tvCount;
            VerticalProgressView verticalProgressView;

            switch (i + 1) {
                case 1: {
                    tvCount = dialogStatisticsBinding.tvCount1;
                    verticalProgressView = dialogStatisticsBinding.verticalProgressView1;
                }
                break;
                case 2: {
                    tvCount = dialogStatisticsBinding.tvCount2;
                    verticalProgressView = dialogStatisticsBinding.verticalProgressView2;
                }
                break;
                case 3: {
                    tvCount = dialogStatisticsBinding.tvCount3;
                    verticalProgressView = dialogStatisticsBinding.verticalProgressView3;
                }
                break;
                case 4: {
                    tvCount = dialogStatisticsBinding.tvCount4;
                    verticalProgressView = dialogStatisticsBinding.verticalProgressView4;
                }
                break;
                case 5: {
                    tvCount = dialogStatisticsBinding.tvCount5;
                    verticalProgressView = dialogStatisticsBinding.verticalProgressView5;
                }
                break;
                case 6: {
                    tvCount = dialogStatisticsBinding.tvCount6;
                    verticalProgressView = dialogStatisticsBinding.verticalProgressView6;
                }
                break;
                default: {
                    tvCount = null;
                    verticalProgressView = null;
                }
                break;
            }

            if (tvCount != null && verticalProgressView != null) {
                tvCount.setText("" + chanceDistribution[i]);
                verticalProgressView.setProgress((int) distributionPercentage);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogStatisticsBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialogStatisticsBinding.btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
        Window dWin = dialog.getWindow();
        if (dWin != null) {
            dWin.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            dWin.setWindowAnimations(R.style.DialogAnimation);
            dWin.setBackgroundDrawable(null);
        }
    }
}
