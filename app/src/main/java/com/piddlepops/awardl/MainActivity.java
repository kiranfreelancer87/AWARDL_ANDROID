package com.piddlepops.awardl;

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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
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
import com.piddlepops.awardl.databinding.ActivityMainBinding;
import com.piddlepops.awardl.databinding.GameOverDialogBinding;
import com.piddlepops.awardl.databinding.GameWinDialogBinding;
import com.piddlepops.awardl.databinding.HintDialogBinding;
import com.piddlepops.awardl.databinding.HintSuccessDialogBinding;
import com.piddlepops.awardl.databinding.ItemviewAdsGoogleBinding;
import com.piddlepops.awardl.databinding.ItemviewButtonsBinding;
import com.piddlepops.awardl.databinding.ItemviewTopBinding;
import com.piddlepops.awardl.databinding.KeyboardBackButtonBinding;
import com.piddlepops.awardl.databinding.KeyboardViewBinding;
import com.piddlepops.awardl.databinding.LetterViewBinding;
import com.piddlepops.awardl.reswords.WordsResponse;
import com.piddlepops.awardl.room.AppDatabase;
import com.piddlepops.awardl.room.HistoryDao;
import com.piddlepops.awardl.room.HistoryEntity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    Handler handler = new Handler();

    int[][] letterGridArray = new int[6][5];

    ArrayList<TextView> InputKeys = new ArrayList<>();

    ArrayList<String> hintList = new ArrayList<>();
    char[] keyBoards = new char[]{'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X', 'C', 'V', 'B', 'N', 'M'};

    ArrayList<TextView> tvSpaces = new ArrayList<>();

    int activePosition = 0;
    int activeRow = 0;

    String correct = "";

    long seconds = 0;
    boolean shouldRunTimer = false;
    private WordsResponse wordsResponse;

    private Dialog dialog;

    private SharedPreferences sharedPreferences;
    private TextView tvTimer;

    private final String TAG = MainActivity.class.getSimpleName();
    private RewardedVideoAd rewardedVideoAd;
    private boolean isLoaded;
    private int retry_count = 0;
    private Runnable runnable;
    private AppDatabase database;
    private HistoryDao historyDao;
    private RewardedAd adMobRewardedAds;

    public void runTimer() {

        long millis = seconds * 1000;
        String time = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

        if (shouldRunTimer) {
            tvTimer.setText(time);
            seconds++;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (handler.hasCallbacks(runnable)) {
                handler.removeCallbacks(runnable);
            }
        } else {
            handler.removeCallbacks(runnable);
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                runTimer();
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("AWARDL", Context.MODE_PRIVATE);


        binding = ActivityMainBinding.inflate(getLayoutInflater());


        setContentView(binding.getRoot());

        database = AppDatabase.getInstance(MainActivity.this);
        historyDao = database.userDao();

        handler = new Handler();
        runnable = this::runTimer;

        loadRewardedVideoAds();
        loadAdmobRewardedAds();

        HintDialogBinding hintDialogBinding = HintDialogBinding.inflate(getLayoutInflater());
        hintDialogBinding.cvCard.setBackground(ContextCompat.getDrawable(this, R.drawable.dialog_gradient));
        dialog = new Dialog(this);
        hintDialogBinding.getRoot().setOnClickListener(view -> dialog.dismiss());
        dialog.setContentView(hintDialogBinding.getRoot());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            binding.getRoot().setRenderEffect(RenderEffect.createBlurEffect(30f, //radius X
                    30f, //Radius Y
                    Shader.TileMode.MIRROR// X=CLAMP,DECAL,MIRROR,REPEAT
            ));
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                shouldRunTimer = true;
                handler.postDelayed(runnable, 1000);
                runTimer();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    binding.getRoot().setRenderEffect(null);
                }
            }
        });

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


    private void initGame() {
        wordsResponse = new WordsResponse();

        wordsResponse.setData(new ArrayList<>(Arrays.asList("apple", "table", "chair", "house", "brush", "shark", "beach", "bread", "flute", "grape", "smile", "quick", "lucky", "jelly", "piano", "zebra", "virus", "inbox", "truck", "tiger", "lemon", "ocean", "stove", "bunny", "jumbo", "daisy", "fairy", "alarm", "camel", "dough", "frogs", "goose", "hotel", "ivory", "juice", "kiwis", "leash", "mango", "novel", "puppy", "quilt", "rhino", "salsa", "toxic", "union", "vines", "wheat", "yacht")));

        //Show Dialog on Success Loading Words
        dialog.show();
        Window dWin = dialog.getWindow();
        dWin.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        dWin.setBackgroundDrawable(null);

        //Init Actions
        setWord();
        initGrid();
        initKeyboard();
    }

    private void setWord() {
        ItemviewTopBinding itemviewTopBinding = ItemviewTopBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
        tvTimer = itemviewTopBinding.tvTimer;
        binding.getRoot().addView(itemviewTopBinding.getRoot());
        AdSettings.addTestDevice("18da483e-115a-4f4e-9742-b07f73bcd6c2");
        LinearLayout adLinearlayout = new LinearLayout(this);
        LinearLayout.LayoutParams ad_lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ad_lp.setMargins(0, 5, 0, 20);
        adLinearlayout.setLayoutParams(ad_lp);
        AdView adView = new AdView(this, "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID", AdSize.BANNER_HEIGHT_50);
        // Add the ad view to your activity layout
        adView.loadAd();
        adLinearlayout.addView(adView);
        // Request an ad
        adView.loadAd();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                loadAdmobRewardedAds();
            }
        });

        //Google Banner Ads
        /*ItemviewAdsGoogleBinding googleBinding = ItemviewAdsGoogleBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
        AdRequest adRequest = new AdRequest.Builder().build();
        googleBinding.adView.loadAd(adRequest);
        binding.getRoot().addView(googleBinding.getRoot());*/


        binding.getRoot().addView(adLinearlayout);
        this.correct = (wordsResponse.getData().toArray()[new Random().nextInt(wordsResponse.getData().size())] + "").toUpperCase(Locale.ROOT);
        this.correct = "apple".toUpperCase();
    }

    private void restartGame() {
        shouldRunTimer = true;
        hintList.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (handler.hasCallbacks(runnable)) {
                shouldRunTimer = true;
                handler.removeCallbacks(runnable);
            }
        } else {
            handler.removeCallbacks(runnable);
        }

        if (runnable != null) {
            shouldRunTimer = true;
            handler.postDelayed(runnable, 1000);
        } else {
            runnable = this::runTimer;
            handler.postDelayed(runnable, 1000);
        }

        //Reset Timer
        seconds = 0;

        wordsResponse = new WordsResponse();
        wordsResponse.setData(new ArrayList<>(Arrays.asList("apple", "table", "chair", "house", "brush", "shark", "beach", "bread", "flute", "grape", "smile", "quick", "lucky", "jelly", "piano", "zebra", "virus", "inbox", "truck", "tiger", "lemon", "ocean", "stove", "bunny", "jumbo", "daisy", "fairy", "alarm", "camel", "dough", "frogs", "goose", "hotel", "ivory", "juice", "kiwis", "leash", "mango", "novel", "puppy", "quilt", "rhino", "salsa", "toxic", "union", "vines", "wheat", "yacht")));
        seconds = 0;
        activePosition = 0;
        shouldRunTimer = true;
        activeRow = 0;
        tvSpaces.clear();
        binding.getRoot().removeAllViews();
        binding.getRoot().invalidate();
        runTimer();
        setWord();
        initGrid();
        initKeyboard();
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
                messageLayout.setOrientation(LinearLayout.HORIZONTAL);
                TextView textView = new TextView(this);
                textView.setText(MessageFormat.format("GUESS THE {0}-LETTER WORD!", letterGridArray[0].length));
                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setTextSize(18f);
                        textView.setTextColor(Color.BLACK);
                    }
                });
                Typeface tf = ResourcesCompat.getFont(this, R.font.gothambold);
                textView.setTypeface(tf);
                messageLayout.addView(textView);
                rowsLinearLayout.addView(messageLayout);
            }

            for (int j = 0; j < letterGridArray[i].length; j++) {
                stringBuilder.append(MessageFormat.format("{0}{1}{2}", i, j, j < (letterGridArray[i].length - 1) ? " " : ""));
                LetterViewBinding letterViewBinding = LetterViewBinding.inflate(getLayoutInflater());
                tvSpaces.add(letterViewBinding.tvLetter);
                columnLinearLayout.addView(letterViewBinding.getRoot());
            }
            if (i < letterGridArray.length - 1) {
                stringBuilder.append("\n");
            }
            rows.append(stringBuilder);
            rowsLinearLayout.addView(columnLinearLayout);
        }
        binding.getRoot().addView(rowsLinearLayout);
    }

    private void initKeyboard() {
        LinearLayout llFooter = new LinearLayout(this);
        llFooter.setOrientation(LinearLayout.VERTICAL);
        llFooter.setBackgroundColor(Color.parseColor("#20FFFFFF"));
        ItemviewButtonsBinding itemviewButtonsBinding = ItemviewButtonsBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
        itemviewButtonsBinding.cvSubmit.setTag("Enter");
        itemviewButtonsBinding.cvHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((rewardedVideoAd != null && rewardedVideoAd.isAdLoaded()) || adMobRewardedAds != null) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("Watch an Ad to get the hint.");
                    dialog.setPositiveButton("Yes", (dialogInterface, i) -> {
                        if (rewardedVideoAd == null || !rewardedVideoAd.isAdLoaded()) {
                            if (adMobRewardedAds != null) {
                                adMobRewardedAds.show(MainActivity.this, new OnUserEarnedRewardListener() {
                                    @Override
                                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                        giveHint();
                                        loadAdmobRewardedAds();
                                    }
                                });
                            }
                            giveHint();
                            return;
                        }
                        // Check if ad is already expired or invalidated, and do not show ad if that is the case. You will not get paid to show an invalidated ad.
                        if (rewardedVideoAd.isAdInvalidated()) {
                            giveHint();
                            return;
                        }
                        rewardedVideoAd.show();
                    });

                    dialog.setNegativeButton("No", (dialogInterface, i) -> {

                    });
                    dialog.show();
                } else {
                    giveHint();
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
        LinearLayout llBottom2 = new LinearLayout(this);
        llBottom2.setGravity(Gravity.CENTER);
        LinearLayout llBottom3 = new LinearLayout(this);
        llBottom3.setGravity(Gravity.CENTER);

        for (int i = 0; i < keyBoards.length; i++) {
            View v;
            View v1 = null;
            if (i < 10) {
                KeyboardViewBinding letterViewBinding = KeyboardViewBinding.inflate(getLayoutInflater());
                v = letterViewBinding.getRoot();
                v.setTag(keyBoards[i]);
                letterViewBinding.tvLetter.setText(String.valueOf(keyBoards[i]));
                InputKeys.add(letterViewBinding.tvLetter);
                llBottom.addView(letterViewBinding.getRoot());
                v.setOnClickListener(this::initOnClick);
            } else if (i < 19) {
                KeyboardViewBinding letterViewBinding = KeyboardViewBinding.inflate(getLayoutInflater());
                letterViewBinding.tvLetter.setText(String.valueOf(keyBoards[i]));
                InputKeys.add(letterViewBinding.tvLetter);
                v = letterViewBinding.getRoot();
                v.setTag(keyBoards[i]);
                llBottom2.addView(letterViewBinding.getRoot());
                v.setOnClickListener(this::initOnClick);
            } else {
                KeyboardViewBinding letterViewBinding = KeyboardViewBinding.inflate(getLayoutInflater());
                v = letterViewBinding.getRoot();
                v.setTag(keyBoards[i]);
                letterViewBinding.tvLetter.setText(String.valueOf(keyBoards[i]));
                InputKeys.add(letterViewBinding.tvLetter);
                llBottom3.addView(letterViewBinding.getRoot());
                v.setOnClickListener(this::initOnClick);
                if (i == keyBoards.length - 1) {
                    v1 = v;
                    KeyboardBackButtonBinding keyboardBackButtonBinding = KeyboardBackButtonBinding.inflate(getLayoutInflater());
                    v = keyboardBackButtonBinding.getRoot();
                    v.setTag("Back");
                    llBottom3.addView(keyboardBackButtonBinding.getRoot());
                    v.setOnClickListener(this::initOnClick);
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
                    GameOverDialogBinding gameOverDialogBinding = GameOverDialogBinding.inflate(getLayoutInflater());
                    gameOverDialogBinding.cvCard.setBackground(ContextCompat.getDrawable(this, R.drawable.dialog_gradient));
                    gameOverDialogBinding.tvSorryMsg.setText(MessageFormat.format("Sorry {0},\nthe word was:", ""));
                    gameOverDialogBinding.tvCorrectWord.setText(correct);
                    gameOverDialogBinding.tvScore.setText(MessageFormat.format("Score: {0}", "10"));

                    //Set History
                    HistoryEntity historyEntity = new HistoryEntity();
                    historyEntity.game_time = System.currentTimeMillis();
                    historyEntity.time_consumed = seconds;
                    historyEntity.attempts = 6;
                    historyEntity.word_len = correct.length();
                    historyEntity.word = "";
                    historyEntity.hint_count = hintList.size();
                    historyDao.insert(historyEntity);
                    Dialog dialog1 = new Dialog(this);
                    gameOverDialogBinding.btnBackToMain.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });

                    gameOverDialogBinding.btnNew.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog1.dismiss();
                            restartGame();
                        }
                    });
                    //gameOverDialogBinding.getRoot().setOnClickListener(view -> dialog1.dismiss());
                    dialog1.setContentView(gameOverDialogBinding.getRoot());
                    dialog1.setCancelable(false);
                    dialog1.show();
                    shouldRunTimer = false;
                    Window dWin = dialog1.getWindow();
                    dWin.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    dWin.setBackgroundDrawable(null);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        binding.getRoot().setRenderEffect(RenderEffect.createBlurEffect(30f, //radius X
                                30f, //Radius Y
                                Shader.TileMode.MIRROR// X=CLAMP,DECAL,MIRROR,REPEAT
                        ));
                    }

                    dialog1.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                binding.getRoot().setRenderEffect(null);
                            }
                        }
                    });
                } else if (checkIfCorrect) {
                    GameWinDialogBinding gameWinDialogBinding = GameWinDialogBinding.inflate(getLayoutInflater());
                    gameWinDialogBinding.cvCard.setBackground(ContextCompat.getDrawable(this, R.drawable.dialog_gradient));
                    gameWinDialogBinding.tvWinMsg.setText(MessageFormat.format("CONGRATULATIONS\n{0}!", ""));

                    //Set History
                    HistoryEntity historyEntity = new HistoryEntity();
                    historyEntity.game_time = System.currentTimeMillis();
                    historyEntity.time_consumed = seconds;
                    historyEntity.attempts = activeRow;
                    historyEntity.word_len = correct.length();
                    historyEntity.word = correct;
                    historyEntity.hint_count = hintList.size();
                    historyDao.insert(historyEntity);

                    float point = 0.0f;
                    long time_diff = 0;
                    if (seconds < 600) {
                        time_diff = 600 - seconds;
                    }

                    switch (activeRow) {
                        case 1 -> point = time_diff + 300;
                        case 2 -> point = time_diff + 200;
                        case 3 -> point = time_diff + 132;
                        case 4 -> point = time_diff + 100;
                        case 5 -> point = time_diff + 80;
                        case 6 -> point = time_diff + 66;
                    }

                    gameWinDialogBinding.tvScore.setText(MessageFormat.format("Score: {0}", point));
                    //postResult(point, activeRow);
                    Dialog dialog1 = new Dialog(this);
                    gameWinDialogBinding.btnBackToMain.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog1.dismiss();
                            restartGame();
                        }
                    });
                    gameWinDialogBinding.btnNew.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog1.dismiss();
                            restartGame();
                        }
                    });
                    //gameWinDialogBinding.getRoot().setOnClickListener(view -> dialog1.dismiss());
                    dialog1.setContentView(gameWinDialogBinding.getRoot());
                    dialog1.setCancelable(false);
                    dialog1.show();
                    shouldRunTimer = false;
                    Window dWin = dialog1.getWindow();
                    dWin.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    dWin.setBackgroundDrawable(null);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        binding.getRoot().setRenderEffect(RenderEffect.createBlurEffect(30f, //radius X
                                30f, //Radius Y
                                Shader.TileMode.MIRROR// X=CLAMP,DECAL,MIRROR,REPEAT
                        ));
                    }

                    dialog1.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                binding.getRoot().setRenderEffect(null);
                            }
                        }
                    });
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

    void loadRewardedVideoAds() {
        rewardedVideoAd = new RewardedVideoAd(this, "YOUR_PLACEMENT_ID");
        RewardedVideoAdListener rewardedVideoAdListener = new RewardedVideoAdListener() {
            @Override
            public void onError(Ad ad, AdError error) {
                if (retry_count < 3) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!rewardedVideoAd.isAdLoaded()) {
                                loadRewardedVideoAds();
                                retry_count = retry_count + 1;
                            }
                        }
                    }, 10000);
                }
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
                loadRewardedVideoAds();
            }

            @Override
            public void onRewardedVideoClosed() {

            }
        };
        rewardedVideoAd.loadAd(rewardedVideoAd.buildLoadAdConfig().withAdListener(rewardedVideoAdListener).build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
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
}
