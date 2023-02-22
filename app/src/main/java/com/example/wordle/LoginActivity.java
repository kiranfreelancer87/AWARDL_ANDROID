package com.example.wordle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.example.wordle.databinding.ActivityLoginBinding;

import java.text.MessageFormat;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.parent.post(() -> {
            AlertDialog.Builder al = new AlertDialog.Builder(LoginActivity.this);
            al.setTitle(MessageFormat.format("Width: {0}, Height: {1}", binding.parent.getWidth(), binding.parent.getHeight()));
            al.show();
        });

        binding.btnLogin.setOnClickListener(view -> {
            if (binding.etName.getText().toString().isEmpty() || !binding.etName.getText().toString().contains(" ")) { binding.etName.setError("Enter Valid First & Last name"); return;
            }
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("name", binding.etName.getText().toString());
            finish();
            startActivity(intent);
        });

    }
}