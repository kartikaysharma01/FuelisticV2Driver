package com.example.fuelisticv2driver.LoginSignUp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.example.fuelisticv2driver.MainActivity;
import com.example.fuelisticv2driver.R;

public class LogoSplash extends AppCompatActivity {

    public static int SPLASH_SCREEN= 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.activity_logo_splash);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run() {
                Intent i =new Intent(LogoSplash.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_SCREEN );
    }

}