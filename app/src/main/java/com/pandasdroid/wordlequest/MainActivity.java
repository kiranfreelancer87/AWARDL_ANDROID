package com.pandasdroid.wordlequest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.pandasdroid.wordlequest.databinding.ActivityMainBinding;
import com.pandasdroid.wordlequest.databinding.AdsContainerBinding;
import com.pandasdroid.wordlequest.databinding.GameClosedLayoutFailureBinding;
import com.pandasdroid.wordlequest.databinding.GameClosedLayoutSuccessBinding;
import com.pandasdroid.wordlequest.databinding.GameOverDialogBinding;
import com.pandasdroid.wordlequest.databinding.GameWinDialogBinding;
import com.pandasdroid.wordlequest.databinding.HintDialogBinding;
import com.pandasdroid.wordlequest.databinding.HintSuccessDialogBinding;
import com.pandasdroid.wordlequest.databinding.ItemviewAdsGoogleBinding;
import com.pandasdroid.wordlequest.databinding.ItemviewButtonsBinding;
import com.pandasdroid.wordlequest.databinding.ItemviewTopBinding;
import com.pandasdroid.wordlequest.databinding.KeyboardBackButtonBinding;
import com.pandasdroid.wordlequest.databinding.KeyboardViewBinding;
import com.pandasdroid.wordlequest.databinding.LetterView26Binding;
import com.pandasdroid.wordlequest.databinding.LetterView30Binding;
import com.pandasdroid.wordlequest.databinding.LetterView34Binding;
import com.pandasdroid.wordlequest.databinding.LetterView38Binding;
import com.pandasdroid.wordlequest.databinding.LetterViewBinding;
import com.pandasdroid.wordlequest.room.AppDatabase;
import com.pandasdroid.wordlequest.room.HistoryDao;
import com.pandasdroid.wordlequest.room.HistoryEntity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;


    int[][] letterGridArray = new int[0][0];

    String definition = "";

    ArrayList<TextView> InputKeys = new ArrayList<>();

    ArrayList<String> hintList = new ArrayList<>();
    char[] keyBoards = new char[]{'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X', 'C', 'V', 'B', 'N', 'M'};

    ArrayList<TextView> tvSpaces = new ArrayList<>();

    int activePosition = 0;
    int activeRow = 0;

    String correct = "";

    private SharedPreferences sharedPreferences;

    private final String TAG = MainActivity.class.getSimpleName();
    private RewardedVideoAd rewardedVideoAd;
    private boolean isLoaded;
    private int retry_count = 0;
    private AppDatabase database;
    private HistoryDao historyDao;
    private RewardedAd adMobRewardedAds;
    private int game_type = -1;
    private boolean isFacebookFailed = false;
    private LinearLayout llFooter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() == null || getIntent().getStringExtra("correct") == null || getIntent().getStringExtra("correct").length() < 4) {
            String selectedValue = getSharedPreferences(GameConstants.Pref_Name, Context.MODE_PRIVATE).getString("selectedValue", "5");
            int wordLength = Integer.parseInt(selectedValue);
            String randomWord = WordUtils.getRandomWord(getApplicationContext(), wordLength);
            if (randomWord != null) {
                correct = randomWord.toUpperCase();
                game_type = 0;
            } else {
                Toast.makeText(this, "No Matching Word Found.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } else {
            this.correct = getIntent().getStringExtra("correct").toUpperCase();
            game_type = 1;
        }

        correct = "APPLE";

        if (definition.isEmpty()) {
            definition = WordUtils.getWordDefinition(MainActivity.this, correct);
        }


        letterGridArray = new int[6][correct.length()];


        sharedPreferences = getSharedPreferences(GameConstants.Pref_Name, Context.MODE_PRIVATE);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getInstance(MainActivity.this);
        historyDao = database.userDao();

        if (AudienceNetworkAds.isInitialized(getApplicationContext())) {
            loadFacebookRewardedVideoAd();
        }
        if (MyApplication.isAdmobInitialized) {
            loadAdmobRewardedAds();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initGame();
    }

    private void loadAdmobRewardedAds() {

        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                adMobRewardedAds = null;
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                adMobRewardedAds = ad;
            }
        });
    }

    private void showHowToPlayDialog() {
        HintDialogBinding hintDialogBinding = HintDialogBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
        androidx.appcompat.app.AlertDialog.Builder dialog_builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
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

    private void initGame() {
        setWordGridAndKeyboard();
    }

    private void setWord() {
        ItemviewTopBinding itemviewTopBinding = ItemviewTopBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
        binding.getRoot().addView(itemviewTopBinding.getRoot());
        itemviewTopBinding.help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHowToPlayDialog();
            }
        });
        AdSettings.addTestDevice("681e317c-c93e-43ce-a887-756044400498");
        LinearLayout adLinearlayout = new LinearLayout(this);
        LinearLayout.LayoutParams ad_lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ad_lp.setMargins(0, 5, 0, 20);
        adLinearlayout.setLayoutParams(ad_lp);
        /*AdView adView = new AdView(this, "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID", AdSize.BANNER_HEIGHT_50);
        // Add the ad view to your activity layout
        adView.loadAd();
        adLinearlayout.addView(adView);
        // Request an ad
        adView.loadAd();*/


        if (MyApplication.isAdmobInitialized) {
            loadAdmobRewardedAds();
        }


        binding.getRoot().addView(adLinearlayout);
    }

    private void restartGame() {
        hintList.clear();
        activePosition = 0;
        activeRow = 0;
        tvSpaces.clear();
        binding.getRoot().removeAllViews();
        binding.getRoot().invalidate();
        setWordGridAndKeyboard();
    }

    private void setWordGridAndKeyboard() {
        setWord();
        initGrid();
        initKeyboard();
        AdsContainerBinding adsContainerBinding = AdsContainerBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
        binding.getRoot().addView(adsContainerBinding.getRoot());
    }

    private void initGrid() {
        StringBuilder rows = new StringBuilder();
        LinearLayout rowsLinearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp_grid = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        rowsLinearLayout.setLayoutParams(lp_grid);
        rowsLinearLayout.setOrientation(LinearLayout.VERTICAL);
        rowsLinearLayout.setGravity(Gravity.CENTER);
        rowsLinearLayout.setHorizontalGravity(Gravity.CENTER);
        rowsLinearLayout.setVerticalGravity(Gravity.CENTER);

        for (int i = 0; i < letterGridArray.length; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            LinearLayout columnLinearLayout = new LinearLayout(this);
            columnLinearLayout.setGravity(Gravity.CENTER);
            columnLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            if (i == 0) {
                LinearLayout messageLayout = new LinearLayout(this);
                messageLayout.setGravity(Gravity.CENTER);
                messageLayout.setVerticalGravity(Gravity.CENTER);
                messageLayout.setOrientation(LinearLayout.HORIZONTAL);
                TextView textView = new TextView(this);
                textView.setText(MessageFormat.format("GUESS THE {0}-LETTER WORD!", letterGridArray[0].length));
                textView.post(() -> {
                    textView.setTextSize(18f);
                    textView.setTextColor(Color.BLACK);
                    textView.setPadding(0, 10, 0, 10);
                });
                Typeface tf = ResourcesCompat.getFont(this, R.font.gothambold);
                textView.setTypeface(tf);
                messageLayout.addView(textView);
                rowsLinearLayout.addView(messageLayout);
            }

            for (int j = 0; j < letterGridArray[i].length; j++) {
                stringBuilder.append(MessageFormat.format("{0}{1}{2}", i, j, j < (letterGridArray[i].length - 1) ? " " : ""));
                if (correct.length() <= 7) {
                    LetterView38Binding letterViewBinding = LetterView38Binding.inflate(getLayoutInflater(), binding.getRoot(), false);
                    tvSpaces.add(letterViewBinding.tvLetter);
                    columnLinearLayout.addView(letterViewBinding.getRoot());
                } else if (correct.length() == 8) {
                    LetterView34Binding letterViewBinding = LetterView34Binding.inflate(getLayoutInflater(), binding.getRoot(), false);
                    tvSpaces.add(letterViewBinding.tvLetter);
                    columnLinearLayout.addView(letterViewBinding.getRoot());
                } else if (correct.length() == 9) {
                    LetterView30Binding letterViewBinding = LetterView30Binding.inflate(getLayoutInflater(), binding.getRoot(), false);
                    tvSpaces.add(letterViewBinding.tvLetter);
                    columnLinearLayout.addView(letterViewBinding.getRoot());
                } else if (correct.length() == 10) {
                    LetterView26Binding letterViewBinding = LetterView26Binding.inflate(getLayoutInflater(), binding.getRoot(), false);
                    tvSpaces.add(letterViewBinding.tvLetter);
                    columnLinearLayout.addView(letterViewBinding.getRoot());
                }
            }
            if (i < letterGridArray.length - 1) {
                stringBuilder.append("\n");
            }
            rows.append(stringBuilder);
            rowsLinearLayout.addView(columnLinearLayout);
        }
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.popup_enter_animation);
        rowsLinearLayout.startAnimation(animation);
        binding.getRoot().addView(rowsLinearLayout);
    }

    private void initKeyboard() {
        llFooter = new LinearLayout(this);
        llFooter.setOrientation(LinearLayout.VERTICAL);
        llFooter.setBackgroundColor(Color.parseColor("#20FFFFFF"));
        ItemviewButtonsBinding itemviewButtonsBinding = ItemviewButtonsBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
        itemviewButtonsBinding.cvSubmit.setTag("Enter");
        itemviewButtonsBinding.cvHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFacebookFailed) {
                    loadFacebookRewardedVideoAd();
                }
                if ((rewardedVideoAd != null && rewardedVideoAd.isAdLoaded()) || adMobRewardedAds != null) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("Watch an Ad to get the hint.");
                    dialog.setPositiveButton("Yes", (dialogInterface, i) -> {
                        if (rewardedVideoAd == null || !rewardedVideoAd.isAdLoaded()) {
                            if (adMobRewardedAds != null) {
                                adMobRewardedAds.show(MainActivity.this, new OnUserEarnedRewardListener() {
                                    @Override
                                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                        adMobRewardedAds = null;
                                        giveHint();
                                        if (MyApplication.isAdmobInitialized) {
                                            loadAdmobRewardedAds();
                                        }
                                        if (AudienceNetworkAds.isInitialized(getApplicationContext())) {
                                            loadFacebookRewardedVideoAd();
                                        }
                                    }
                                });
                            }
                            return;
                        } else if (rewardedVideoAd.isAdLoaded()) {
                            // Check if ad is already expired or invalidated, and do not show ad if that is the case. You will not get paid to show an invalidated ad.
                            if (rewardedVideoAd.isAdInvalidated()) {
                                giveHint();
                                return;
                            }
                            rewardedVideoAd.show();
                        } else {
                            giveHint();
                        }
                    });

                    dialog.setNegativeButton("No", (dialogInterface, i) -> {

                    });
                    dialog.show();
                } else {
                    giveHint();
                    if (MyApplication.isAdmobInitialized) {
                        loadAdmobRewardedAds();
                    }
                    if (AudienceNetworkAds.isInitialized(getApplicationContext())) {
                        loadFacebookRewardedVideoAd();
                    }
                }
            }
        });
        itemviewButtonsBinding.cvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initOnClick(view);
            }
        });
        itemviewButtonsBinding.cvRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("Do you want to restart?");
                dialog.setPositiveButton("Yes", (dialogInterface, i) -> {
                    restartGame();
                });
                dialog.setNegativeButton("No", (dialogInterface, i) -> {

                });
                dialog.show();
            }
        });
        //Buttons
        llFooter.addView(itemviewButtonsBinding.getRoot());

        LinearLayout llBottom = new LinearLayout(this);
        llBottom.setGravity(Gravity.CENTER);
        llBottom.setPadding(0, 0, 0, 5);
        LinearLayout llBottom2 = new LinearLayout(this);
        llBottom2.setGravity(Gravity.CENTER);
        llBottom2.setPadding(0, 0, 0, 5);
        LinearLayout llBottom3 = new LinearLayout(this);
        llBottom3.setGravity(Gravity.CENTER);
        llBottom3.setPadding(0, 0, 0, 5);

        for (int i = 0; i < keyBoards.length; i++) {
            View v;
            View v1 = null;
            if (i < 10) {
                KeyboardViewBinding letterViewBinding = KeyboardViewBinding.inflate(getLayoutInflater());
                v = letterViewBinding.tvLetter;
                v.setTag(keyBoards[i]);
                letterViewBinding.tvLetter.setText(String.valueOf(keyBoards[i]));
                InputKeys.add(letterViewBinding.tvLetter);
                llBottom.addView(letterViewBinding.getRoot());
                v.setOnClickListener(this::initOnClick);
                View finalV3 = v;
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.wtf("Vh:Vw", finalV3.getHeight() + "," + finalV3.getWidth());

                    }
                });
            } else if (i < 19) {
                KeyboardViewBinding letterViewBinding = KeyboardViewBinding.inflate(getLayoutInflater());
                letterViewBinding.tvLetter.setText(String.valueOf(keyBoards[i]));
                InputKeys.add(letterViewBinding.tvLetter);
                v = letterViewBinding.tvLetter;
                v.setTag(keyBoards[i]);
                llBottom2.addView(letterViewBinding.getRoot());
                v.setOnClickListener(this::initOnClick);
                View finalV2 = v;
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.wtf("Vh:Vw", finalV2.getHeight() + "," + finalV2.getWidth());

                    }
                });
            } else {
                KeyboardViewBinding letterViewBinding = KeyboardViewBinding.inflate(getLayoutInflater());
                v = letterViewBinding.tvLetter;
                v.setTag(keyBoards[i]);
                letterViewBinding.tvLetter.setText(String.valueOf(keyBoards[i]));
                InputKeys.add(letterViewBinding.tvLetter);
                llBottom3.addView(letterViewBinding.getRoot());
                v.setOnClickListener(this::initOnClick);
                View finalV1 = v;
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.wtf("Vh:Vw", finalV1.getHeight() + "," + finalV1.getWidth());

                    }
                });
                if (i == keyBoards.length - 1) {
                    v1 = v;
                    KeyboardBackButtonBinding keyboardBackButtonBinding = KeyboardBackButtonBinding.inflate(getLayoutInflater());
                    v = keyboardBackButtonBinding.getRoot();
                    v.setTag("Back");
                    llBottom3.addView(keyboardBackButtonBinding.getRoot());
                    v.setOnClickListener(this::initOnClick);
                    View finalV = v;
                    v.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.wtf("Vh:Vw", finalV.getHeight() + "," + finalV.getWidth());
                        }
                    });
                }
            }

            Log.wtf("Tag...", v.getTag().toString() + " " + i);

            View popupView = getLayoutInflater().inflate(R.layout.layout_key_press, binding.getRoot(), false);
            TextView tvKeyPress = popupView.findViewById(R.id.tvKeyPress);
            PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            if (i == keyBoards.length - 1) {
                v = v1;
            }

            v.setOnTouchListener(new View.OnTouchListener() {
                int x, y;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        tvKeyPress.setText(v.getTag().toString()); // Set the key press value dynamically

                        int[] location = new int[2];
                        v.getLocationOnScreen(location);

                        x = location[0]; // x-coordinate
                        y = location[1]; // y-coordinate

                        popupWindow.showAtLocation(v, Gravity.TOP | Gravity.START, x, y - (v.getHeight()));
                        v.performClick();
                        return true; // Return true to consume the event
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        popupWindow.dismiss();
                        return true; // Return true to consume the event
                    }
                    return false; // Return false to allow normal touch behavior for other events
                }
            });

        }

        //Keys Row 1
        llFooter.addView(llBottom);
        //Keys Row 2
        llFooter.addView(llBottom2);
        //Keys Row 3
        llFooter.addView(llBottom3);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.popup_enter_animation);
        llFooter.startAnimation(animation);

        //Adding Footer View to the LinearLayout
        binding.getRoot().addView(llFooter);
    }

    private void giveHint() {
        if (correct.length() != hintList.size()) {
            char c = correct.toCharArray()[hintList.size()];
            for (int i = 0; i < InputKeys.size(); i++) {
                if (c == InputKeys.get(i).getText().toString().toCharArray()[0]) {
                    InputKeys.get(i).setBackgroundColor(getColor(R.color.green));
                    InputKeys.get(i).setTextColor(Color.WHITE);
                    AlphaAnimation blinkAnimation = new AlphaAnimation(1.0f, 0.0f); // Create the blink animation
                    blinkAnimation.setDuration(500); // Set the duration of each animation cycle
                    blinkAnimation.setRepeatMode(Animation.REVERSE); // Reverse the animation on each cycle
                    blinkAnimation.setRepeatCount(2); // Set the number of times the animation should repeat (infinite in this case)
                    InputKeys.get(i).startAnimation(blinkAnimation); // Start the blink animation
                    hintList.add("" + c);
                    showHintDialog();
                    return;
                }
            }
        }
        showHintDialog();
    }

    private void showHintDialog() {
        androidx.appcompat.app.AlertDialog.Builder dialog1 = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < letterGridArray[0].length; i++) {
            if (i < hintList.size()) {
                stringBuilder.append(hintList.get(i));
            } else {
                stringBuilder.append("*");
            }
        }

        HintSuccessDialogBinding hintSuccessDialogBinding = HintSuccessDialogBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
        hintSuccessDialogBinding.tvHint.setText(stringBuilder);
        hintSuccessDialogBinding.tvDefinition.setText(definition);
        dialog1.setView(hintSuccessDialogBinding.getRoot());
        androidx.appcompat.app.AlertDialog dialog_ = dialog1.create();
        dialog_.setCancelable(false);
        hintSuccessDialogBinding.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_.dismiss();
            }
        });
        // Set the background color to transparent
        dialog_.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_.show();
    }

    public void scaleView(View v, float startScale, float endScale) {
        Animation anim = new ScaleAnimation(1f, 1f, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 1f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(300);
        v.startAnimation(anim);
    }

    public void scaleDownView(View v, float startScale, float endScale) {
        Animation anim = new ScaleAnimation(1f, 1f, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 1f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(300);
        v.startAnimation(anim);
    }

    private void initOnClick(View v) {
        Log.wtf("InitOnClick", "" + v.getTag());

        if (v.getTag().toString().equals("Enter")) {
            if (activePosition < (activeRow + 1) * letterGridArray[0].length) {
                return;
            }
            if (activePosition % letterGridArray[0].length == 0) {
                boolean checkIfCorrect = true;
                int counter = 0;
                if (correct.toCharArray().length != letterGridArray[0].length) {
                    return;
                }

                for (int i = activeRow * letterGridArray[0].length; i < (activeRow + 1) * letterGridArray[0].length; i++) {
                    TextView viewToAnimate = tvSpaces.get(i);

                    if (!(correct.toCharArray()[counter] == viewToAnimate.getText().toString().toCharArray()[0])) {
                        checkIfCorrect = false;
                    }

                    if (correct.toCharArray()[counter] == viewToAnimate.getText().toString().toCharArray()[0]) {
                        LinearLayout linearLayout = (LinearLayout) viewToAnimate.getParent();
                        linearLayout.setBackgroundColor(getColor(R.color.green));
                    } else {
                        LinearLayout linearLayout = (LinearLayout) viewToAnimate.getParent();
                        linearLayout.setBackgroundColor(getColor(R.color.darkend_Yellow));

                        char activeChar = viewToAnimate.getText().toString().toCharArray()[0];

                        ArrayList<Integer> activeRepeatCountPositions = new ArrayList<>();

                        int correctCharCount = 0;

                        ArrayList<Integer> correctCharsPos = new ArrayList<>();

                        for (int j = activeRow * letterGridArray[0].length; j < (activeRow + 1) * letterGridArray[0].length; j++) {
                            if (tvSpaces.get(j).getText().charAt(0) == activeChar) {
                                activeRepeatCountPositions.add(j);
                            }
                        }

                        for (int ci = 0; ci < correct.toCharArray().length; ci++) {
                            if (correct.toCharArray()[ci] == activeChar) {
                                correctCharCount++;
                                correctCharsPos.add((activeRow * letterGridArray[0].length) + ci);
                            }
                        }

                        for (int correctPos : correctCharsPos) {
                            activeRepeatCountPositions.remove((Object) correctPos);
                        }

                        int filledCount = 0;

                        for (int cpi = 0; cpi < correctCharsPos.size(); cpi++) {
                            if (activeChar == tvSpaces.get(correctCharsPos.get(cpi)).getText().charAt(0)) {
                                filledCount++;
                            }
                        }

                        if (filledCount == correctCharsPos.size()) {
                            linearLayout.setBackgroundColor(getColor(R.color.gray));
                        }

                        int tobeReplacedCount = correctCharCount - filledCount;

                        for (int ii = 0; ii < activeRepeatCountPositions.size(); ii++) {
                            if (ii < tobeReplacedCount) {
                                ((LinearLayout) tvSpaces.get(activeRepeatCountPositions.get(ii)).getParent()).setBackgroundColor(getColor(R.color.darkend_Yellow));
                            } else {
                                ((LinearLayout) tvSpaces.get(activeRepeatCountPositions.get(ii)).getParent()).setBackgroundColor(getColor(R.color.gray));
                            }
                        }
                    }

                    counter++;
                    new Handler().postDelayed(() -> {
                        viewToAnimate.animate().withLayer().rotationY(90).setDuration(300).withEndAction(() -> {
                            // second quarter turn
                            viewToAnimate.setRotationY(-90);
                            viewToAnimate.animate().withLayer().rotationY(0).setDuration(300).start();
                        }).start();
                    }, 300L * counter);
                }

                activeRow++;

                if (!checkIfCorrect && activeRow == letterGridArray.length) {
                    //Set History
                    HistoryEntity historyEntity = new HistoryEntity();
                    historyEntity.game_time = System.currentTimeMillis();
                    historyEntity.game_type = game_type;
                    historyEntity.time_consumed = 0;
                    historyEntity.attempts = 6;
                    historyEntity.word_len = correct.length();
                    historyEntity.win = false;
                    historyEntity.word = correct;
                    historyEntity.hint_count = hintList.size();
                    historyDao.insert(historyEntity);

                    llFooter.removeAllViews();

                    GameClosedLayoutFailureBinding buttonsBinding = GameClosedLayoutFailureBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.popup_enter_animation);
                    buttonsBinding.getRoot().startAnimation(animation);
                    llFooter.addView(buttonsBinding.getRoot());
                    buttonsBinding.btnHome.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });
                    buttonsBinding.btnRetry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            restartGame();
                        }
                    });
                } else if (checkIfCorrect) {
                    //Set History
                    HistoryEntity historyEntity = new HistoryEntity();
                    historyEntity.game_time = System.currentTimeMillis();
                    historyEntity.game_type = game_type;
                    historyEntity.time_consumed = 0;
                    historyEntity.attempts = activeRow;
                    historyEntity.word_len = correct.length();
                    historyEntity.word = correct;
                    historyEntity.win = true;
                    historyEntity.hint_count = hintList.size();
                    historyDao.insert(historyEntity);

                    llFooter.removeAllViews();

                    if (llFooter != null) {
                        GameClosedLayoutSuccessBinding buttonsBinding = GameClosedLayoutSuccessBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
                        llFooter.addView(buttonsBinding.getRoot());
                        Animation animation = AnimationUtils.loadAnimation(this, R.anim.popup_enter_animation);
                        buttonsBinding.getRoot().startAnimation(animation);
                        buttonsBinding.btnHome.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        });
                        buttonsBinding.btnNext.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                recreate();
                            }
                        });
                    }
                }
            }
        } else if (v.getTag().toString().equals("Back")) {
            if (activePosition > 0 && activePosition > (activeRow * letterGridArray[0].length)) {
                activePosition--;
                tvSpaces.get(activePosition).setText("");
                scaleDownView(tvSpaces.get(activePosition), 1f, 0f);
            }
        } else if (activePosition < tvSpaces.size() && activePosition < ((activeRow + 1) * letterGridArray[0].length)) {
            tvSpaces.get(activePosition).setText("" + v.getTag());
            scaleView(tvSpaces.get(activePosition), 0f, 1f);
            activePosition++;
        }
    }

    void loadFacebookRewardedVideoAd() {
        rewardedVideoAd = new RewardedVideoAd(this, "YOUR_PLACEMENT_ID");
        RewardedVideoAdListener rewardedVideoAdListener = new RewardedVideoAdListener() {
            @Override
            public void onError(Ad ad, AdError error) {
                isFacebookFailed = true;
            }

            @Override
            public void onAdLoaded(Ad ad) {
                isLoaded = true;
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Rewarded video ad clicked
                Log.d(TAG, "Rewarded video ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Rewarded Video ad impression - the event will fire when the
                // video starts playing
                Log.d(TAG, "Rewarded video ad impression logged!");
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Do you want to exit?");
        dialog.setPositiveButton("Yes", (dialogInterface, i) -> {
            super.onBackPressed();
        });
        dialog.setNegativeButton("No", (dialogInterface, i) -> {

        });
        dialog.show();
    }

    //Scoring Logic
    private static final Map<Integer, Integer> baseScores;
    private static final int bonusScore = 15;

    static {
        baseScores = new HashMap<>();
        baseScores.put(4, 10);
        baseScores.put(5, 20);
        baseScores.put(6, 30);
        baseScores.put(7, 40);
        baseScores.put(8, 50);
        baseScores.put(9, 60);
        baseScores.put(10, 70);
    }

    public static int calculateScore(String word, int maxTries, int incorrectGuesses, int correctGuesses) {
        int length = word.length();

        // Check if the word length is valid
        if (baseScores.containsKey(length)) {
            int baseScore = baseScores.get(length);

            // Calculate the deduction for incorrect guesses using an exponential decay function
            double incorrectGuessDeduction = baseScore * Math.pow(0.5, incorrectGuesses);

            // Calculate the bonus score for correct guesses
            int correctGuessBonus = bonusScore * correctGuesses;

            // Calculate the final score
            int finalScore = (int) (baseScore - incorrectGuessDeduction + correctGuessBonus);
            finalScore = Math.max(finalScore, 0);  // Ensure score is not negative

            return finalScore;
        } else {
            return 0;  // Invalid word length, return 0 score
        }
    }

    //Level System
    public static String getLevel(int score) {
        if (score >= 200) {
            return "Level 5 - Master";
        } else if (score >= 150) {
            return "Level 4 - Advanced";
        } else if (score >= 100) {
            return "Level 3 - Intermediate";
        } else if (score >= 50) {
            return "Level 2 - Beginner";
        } else {
            return "Level 1 - Novice";
        }
    }
}
