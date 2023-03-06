package com.example.wordle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.example.wordle.databinding.ActivityLoginBinding;

import java.text.MessageFormat;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivCustomLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://piddlepops.com"));
                startActivity(intent);
            }
        });

        binding.btnLogin.setOnClickListener(view -> {
            if (binding.etName.getText().toString().isEmpty() || !binding.etName.getText().toString().contains(" ")) {
                binding.etName.setError("Enter Valid First & Last name");
                return;
            }
            if (binding.etEmail.getText().toString().isEmpty() || !binding.etEmail.getText().toString().matches(emailPattern)) {
                binding.etEmail.setError("Enter Valid Email Address");
                return;
            }

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("name", binding.etName.getText().toString());
            intent.putExtra("email", binding.etEmail.getText().toString());
            finish();
            startActivity(intent);
        });
    }
}