package com.example.wordle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wordle.databinding.ActivityMainBinding;
import com.example.wordle.databinding.GameOverDialogBinding;
import com.example.wordle.databinding.GameWinDialogBinding;
import com.example.wordle.databinding.HintDialogBinding;
import com.example.wordle.databinding.KeyboardBackButtonBinding;
import com.example.wordle.databinding.KeyboardEnterButtonBinding;
import com.example.wordle.databinding.KeyboardViewBinding;
import com.example.wordle.databinding.LetterViewBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    int[][] letterGridArray = new int[6][5];

    char[] keyBoards = new char[]{'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X', 'C', 'V', 'B', 'N', 'M'};

    ArrayList<TextView> tvSpaces = new ArrayList<>();

    int activePosition = 0;
    int activeRow = 0;

    String correct = "";

    String name = "";
    String email;

    long seconds = 0;
    boolean shouldRunTimer = false;
    private TextView tvTimer;

    public void runTimer() {
        if (shouldRunTimer) {
            long millis = seconds * 1000;
            String time = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

            tvTimer.setText(time);
            seconds++;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    runTimer();
                }
            }, 1000);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());


        setContentView(binding.getRoot());

        if (getIntent().getStringExtra("name") != null && getIntent().getStringExtra("email") != null) {
            this.name = getIntent().getStringExtra("name");
            this.email = getIntent().getStringExtra("email");
        } else {
            finish();
        }

        HintDialogBinding hintDialogBinding = HintDialogBinding.inflate(getLayoutInflater());
        hintDialogBinding.cvCard.setBackground(ContextCompat.getDrawable(this, R.drawable.dialog_gradient));
        Dialog dialog = new Dialog(this);
        hintDialogBinding.getRoot().setOnClickListener(view -> dialog.dismiss());
        dialog.setContentView(hintDialogBinding.getRoot());
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                shouldRunTimer = true;
                runTimer();
            }
        });
        dialog.show();

        ColorDrawable cd = new ColorDrawable();
        cd.setColor(Color.parseColor("#98FFFFFF"));
        Window dWin = dialog.getWindow();
        dWin.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        dWin.setBackgroundDrawable(cd);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initGame();
    }

    private void initGame() {
        setWord();
        initImage();
        initGrid();
        initBlankSpace();
        initKeyboard();
        initFooter();
    }

    private void initImage() {
        RelativeLayout relativeLayout = new RelativeLayout(this);
        ImageView imageView = new ImageView(this);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        imageView.setLayoutParams(new ViewGroup.LayoutParams((displayMetrics.widthPixels / 10) * 2, (displayMetrics.widthPixels / 10)));
        imageView.setImageResource(R.drawable.mptftoplogo);
        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        relativeLayout.setGravity(RelativeLayout.CENTER_VERTICAL);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(imageView);
        linearLayout.setGravity(Gravity.CENTER);
        relativeLayout.addView(linearLayout);
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams timerLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(timerLp);
        timerLp.setMargins(15, 15, 0, 0);
        textView.setText("00:00");
        this.tvTimer = textView;
        Typeface timerTypeFace = getResources().getFont(R.font.opensans_bold);
        textView.setTypeface(timerTypeFace);
        textView.setTextColor(Color.BLACK);
        relativeLayout.addView(textView);

        View topMargin = new View(this);
        topMargin.setLayoutParams(new ViewGroup.LayoutParams(displayMetrics.widthPixels / 8, ((displayMetrics.widthPixels / 8) / 4) / 2));
        binding.getRoot().addView(topMargin);
        binding.getRoot().addView(relativeLayout);
        View viewAfterImage = new View(this);
        viewAfterImage.setLayoutParams(new ViewGroup.LayoutParams(displayMetrics.widthPixels / 8, ((displayMetrics.widthPixels / 8) / 4) / 2));
        binding.getRoot().addView(viewAfterImage);
    }

    private void setWord() {
        String[] strings = new String[]{"Scene", "Films", "Movie", "Flick", "Props", "Actor", "Track", "Short", "Light", "Grips", "Shoot", "Rerun", "Indie", "Stage", "Reels", "Video", "Roles", "Epics", "Stunt", "Score", "Takes", "Clamp", "Dolly", "Cameo", "Edits", "Booms", "Extra", "Focus", "Frame", "Title", "Pitch", "Alien", "Ariel", "Crash", "Doubt", "Dumbo", "Earth", "Evita", "Fargo", "Ghost", "Giant", "Greed", "Hotel", "Rambo", "Rocky", "Yentl", "Reels", "Pizza", "Boozy", "Crazy", "Joker", "Shrek", "Dozen", "Fluke", "Caste", "Cache", "Squid", "Champ", "Boxer", "Jaded", "Queen", "Zorro", "Phony", "Bambi", "Speed", "Hitch", "Taken", "Click", "Blade", "Signs", "Crash", "Holes", "Frida", "Radio", "Crank", "Honey", "Babel", "Duets", "Shine", "Awake", "Nixon", "Scoop", "Heist", "Glory", "Venom", "Ponyo", "Brave", "Mulan", "Great", "Scifi", "Drama", "Bones", "Arrow", "Psych", "CHIPS", "Alias", "Louie", "Daria", "Weeds", "Chuck", "Suits", "Angel", "Grimm", "Greek", "Skins", "Haven", "Wacky", "Conan", "Kojak", "Maude", "Plays", "Debut", "Trade", "Viral", "rerun"};
        this.correct = strings[new Random().nextInt(strings.length)].toUpperCase(Locale.ROOT);
        AlertDialog.Builder al = new AlertDialog.Builder(MainActivity.this);
        al.setTitle(MessageFormat.format("Correct word is {0}.", correct));
        al.show();
    }

    private void restartGame() {
        seconds = 0;
        activePosition = 0;
        shouldRunTimer = true;
        activeRow = 0;
        tvSpaces.clear();
        binding.getRoot().removeAllViews();
        binding.getRoot().invalidate();
        runTimer();
        initImage();
        setWord();
        initGrid();
        initBlankSpace();
        initKeyboard();
        initFooter();
    }

    private void initBlankSpace() {
        View gap = new View(this);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        gap.setLayoutParams(new LinearLayout.LayoutParams(displayMetrics.widthPixels / 8, ((displayMetrics.widthPixels / 10))));
        binding.getRoot().addView(gap);
    }

    private void initFooter() {
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.custommadepiddlepop);
        binding.getRoot().addView(imageView);
    }

    private void initGrid() {
        StringBuilder rows = new StringBuilder();
        LinearLayout rowsLinearLayout = new LinearLayout(this);
        rowsLinearLayout.setOrientation(LinearLayout.VERTICAL);

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
                Typeface tf = ResourcesCompat.getFont(this, R.font.opensans_bold);
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
        LinearLayout llBottom = new LinearLayout(this);
        llBottom.setGravity(Gravity.CENTER);
        LinearLayout llBottom2 = new LinearLayout(this);
        llBottom2.setGravity(Gravity.CENTER);
        LinearLayout llBottom3 = new LinearLayout(this);
        llBottom3.setGravity(Gravity.CENTER);

        for (int i = 0; i < keyBoards.length; i++) {
            View v;
            if (i < 10) {
                KeyboardViewBinding letterViewBinding = KeyboardViewBinding.inflate(getLayoutInflater());
                v = letterViewBinding.getRoot();
                v.setTag(keyBoards[i]);
                letterViewBinding.tvLetter.setText(String.valueOf(keyBoards[i]));
                llBottom.addView(letterViewBinding.getRoot());
                v.setOnClickListener(this::initOnClick);
            } else if (i < 19) {
                KeyboardViewBinding letterViewBinding = KeyboardViewBinding.inflate(getLayoutInflater());
                letterViewBinding.tvLetter.setText(String.valueOf(keyBoards[i]));
                v = letterViewBinding.getRoot();
                v.setTag(keyBoards[i]);
                llBottom2.addView(letterViewBinding.getRoot());
                v.setOnClickListener(this::initOnClick);
            } else {
                if (i == 19) {
                    KeyboardEnterButtonBinding keyboardEnterButtonBinding = KeyboardEnterButtonBinding.inflate(getLayoutInflater());
                    v = keyboardEnterButtonBinding.getRoot();
                    v.setTag("Enter");
                    llBottom3.addView(keyboardEnterButtonBinding.getRoot());
                    v.setOnClickListener(this::initOnClick);
                }
                KeyboardViewBinding letterViewBinding = KeyboardViewBinding.inflate(getLayoutInflater());
                v = letterViewBinding.getRoot();
                v.setTag(keyBoards[i]);
                letterViewBinding.tvLetter.setText(String.valueOf(keyBoards[i]));
                llBottom3.addView(letterViewBinding.getRoot());
                v.setOnClickListener(this::initOnClick);
                if (i == keyBoards.length - 1) {
                    KeyboardBackButtonBinding keyboardBackButtonBinding = KeyboardBackButtonBinding.inflate(getLayoutInflater());
                    v = keyboardBackButtonBinding.getRoot();
                    v.setTag("Back");
                    llBottom3.addView(keyboardBackButtonBinding.getRoot());
                    v.setOnClickListener(this::initOnClick);
                }
            }
        }

        binding.getRoot().addView(llBottom);
        binding.getRoot().addView(llBottom2);
        binding.getRoot().addView(llBottom3);
    }

    public void scaleView(View v, float startScale, float endScale) {
        Animation anim = new ScaleAnimation(
                1f, 1f, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 1f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(300);
        v.startAnimation(anim);
    }

    public void scaleDownView(View v, float startScale, float endScale) {
        Animation anim = new ScaleAnimation(
                1f, 1f, // Start and end values for the X axis scaling
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
                        if (correct.contains(viewToAnimate.getText().toString())) {
                            LinearLayout linearLayout = (LinearLayout) viewToAnimate.getParent();
                            linearLayout.setBackgroundColor(getColor(R.color.darkend_Yellow));
                        } else {
                            LinearLayout linearLayout = (LinearLayout) viewToAnimate.getParent();
                            linearLayout.setBackgroundColor(getColor(R.color.gray));
                        }
                    }
                    counter++;
                    new Handler().postDelayed(() -> {
                        viewToAnimate.animate().withLayer()
                                .rotationY(90)
                                .setDuration(300)
                                .withEndAction(
                                        () -> {
                                            // second quarter turn
                                            viewToAnimate.setRotationY(-90);
                                            viewToAnimate.animate().withLayer()
                                                    .rotationY(0)
                                                    .setDuration(300)
                                                    .start();
                                        }
                                ).start();
                    }, 300L * counter);
                }
                activeRow++;
                if (!checkIfCorrect && activeRow == letterGridArray.length) {
                    GameOverDialogBinding gameOverDialogBinding = GameOverDialogBinding.inflate(getLayoutInflater());
                    gameOverDialogBinding.cvCard.setBackground(ContextCompat.getDrawable(this, R.drawable.dialog_gradient));
                    gameOverDialogBinding.tvSorryMsg.setText(MessageFormat.format("Sorry  {0},\nthe  word  was:", name));
                    gameOverDialogBinding.tvCorrectWord.setText(correct);
                    gameOverDialogBinding.tvScore.setText(MessageFormat.format("Score: {0}", "0"));
                    postResult(0, activeRow);
                    Dialog dialog1 = new Dialog(this);
                    gameOverDialogBinding.btnBackToMain.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog1.dismiss();
                            finish();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
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
                    ColorDrawable cd = new ColorDrawable();
                    cd.setColor(Color.parseColor("#98FFFFFF"));
                    Window dWin = dialog1.getWindow();
                    dWin.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    dWin.setBackgroundDrawable(cd);
                } else if (checkIfCorrect) {
                    GameWinDialogBinding gameWinDialogBinding = GameWinDialogBinding.inflate(getLayoutInflater());
                    gameWinDialogBinding.cvCard.setBackground(ContextCompat.getDrawable(this, R.drawable.dialog_gradient));
                    gameWinDialogBinding.tvWinMsg.setText(MessageFormat.format("CONGRATULATIONS\n{0}!", name));
                    float point = 0.0f;
                    if (seconds < 45) {
                        switch (activeRow) {
                            case 1:
                            case 2:
                                point = 200;
                                break;
                            case 3:
                                point = 132;
                                break;
                            case 4:
                                point = 100;
                                break;
                            case 5:
                                point = 80;
                                break;
                            case 6:
                                point = 66;
                                break;
                        }
                    } else if (seconds < 90) {
                        switch (activeRow) {
                            case 1:
                            case 2:
                                point = 150;
                                break;
                            case 3:
                                point = 99;
                                break;
                            case 4:
                                point = 75;
                                break;
                            case 5:
                                point = 60;
                                break;
                            case 6:
                                point = 49.5f;
                                break;
                        }
                    } else if (seconds <= 150) {
                        switch (activeRow) {
                            case 1:
                            case 2:
                                point = 100;
                                break;
                            case 3:
                                point = 66;
                                break;
                            case 4:
                                point = 50;
                                break;
                            case 5:
                                point = 40;
                                break;
                            case 6:
                                point = 33;
                                break;
                        }
                    } else if (seconds <= 210) {
                        switch (activeRow) {
                            case 1:
                            case 2:
                                point = 75;
                                break;
                            case 3:
                                point = 49.5f;
                                break;
                            case 4:
                                point = 37.5f;
                                break;
                            case 5:
                                point = 30;
                                break;
                            case 6:
                                point = 24.75f;
                                break;
                        }
                    } else {
                        switch (activeRow) {
                            case 1:
                            case 2:
                                point = 50;
                                break;
                            case 3:
                                point = 33;
                                break;
                            case 4:
                                point = 25;
                                break;
                            case 5:
                                point = 20;
                                break;
                            case 6:
                                point = 16.5f;
                                break;
                        }
                    }

                    gameWinDialogBinding.tvScore.setText(MessageFormat.format("Score: {0}", point));
                    postResult(point, activeRow);
                    Dialog dialog1 = new Dialog(this);
                    gameWinDialogBinding.btnBackToMain.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog1.dismiss();
                            finish();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
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
                    ColorDrawable cd = new ColorDrawable();
                    cd.setColor(Color.parseColor("#98FFFFFF"));
                    Window dWin = dialog1.getWindow();
                    dWin.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    dWin.setBackgroundDrawable(cd);
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

    private void postResult(float point, int noOfGuesses) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(APIInterface.BASE_URL).addConverterFactory(GsonConverterFactory.create(new Gson())).build();
        APIInterface api = retrofit.create(APIInterface.class);
        PostResultModel postBody = new PostResultModel(name, email, seconds, noOfGuesses, point);
        api.postResult(postBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    response.body().string();
                } catch (Exception e) {
                    Snackbar.make(binding.getRoot(), "" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Snackbar.make(binding.getRoot(), "" + t.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
