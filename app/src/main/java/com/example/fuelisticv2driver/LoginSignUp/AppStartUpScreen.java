package com.example.fuelisticv2driver.LoginSignUp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.fuelisticv2driver.R;

public class AppStartUpScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_app_start_up_screen);

    }

    public void callSignUpScreenFromStart(View view) {
        Intent intent = new Intent(AppStartUpScreen.this, SignUp.class);
        startActivity(intent);
    }

    public void callLoginScreenFromStart(View view) {
        Intent intent = new Intent(AppStartUpScreen.this, Login.class);
        startActivity(intent);
    }


}