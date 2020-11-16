package com.example.fuelisticv2driver.LoginSignUp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.example.fuelisticv2driver.R;
import com.google.android.material.textfield.TextInputLayout;

public class SignUp extends AppCompatActivity {
    //variables for getting data
    TextInputLayout fullName, username, licensePlate, password, aadhaar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.activity_sign_up);

        fullName = findViewById(R.id.signUp_full_name);
        username = findViewById(R.id.signUp_username);
        licensePlate = findViewById(R.id.signUp_licensePlate);
        password = findViewById(R.id.signUp_password);
        aadhaar = findViewById(R.id.signUp_aadhaar);
    }

    public void callLoginScreen(View view) {
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }

    public void callNextSignUpScreen(View view) {
        if (!validateFullName() | !validateUsername() | !validateEmail() | !validatePassword() | !validateAadhaar()) {
            return;
        }

        String _fullName = fullName.getEditText().getText().toString();
        String _username = username.getEditText().getText().toString();
        String _licensePlate = licensePlate.getEditText().getText().toString();
        String _password = password.getEditText().getText().toString();
        String _aadhaar = aadhaar.getEditText().getText().toString();

        Intent intent = new Intent(getApplicationContext(), SignUp2ndScreen.class);
        intent.putExtra("fullName", _fullName);
        intent.putExtra("licensePlate", _licensePlate);
        intent.putExtra("username", _username);
        intent.putExtra("password", _password);
        intent.putExtra("aadhaar", _aadhaar);

        startActivity(intent);
        finish();

    }

    private boolean validateFullName() {
        String val = fullName.getEditText().getText().toString().trim();
        if (val.isEmpty()) {
            fullName.setError("Field can not be empty");
            return false;
        } else {
            fullName.setError(null);
            fullName.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateAadhaar() {
        String val = aadhaar.getEditText().getText().toString().trim();
        if (val.isEmpty()) {
            aadhaar.setError("Field can not be empty");
            return false;
        } else if (val.length() < 12) {
            aadhaar.setError("Enter valid Aadhaar number!!");
            return false;
        }else {
            aadhaar.setError(null);
            aadhaar.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateUsername() {
        String val = username.getEditText().getText().toString().trim();
        String checkSpaces = "\\A\\w{1,20}\\z";

        if (val.isEmpty()) {
            username.setError("Field can not be empty");
            return false;
        } else if (val.length() > 20) {
            username.setError("Username is too large!");
            return false;
        } else if (!val.matches(checkSpaces)) {
            username.setError("No White spaces are allowed!");
            return false;
        } else {
            username.setError(null);
            username.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateEmail() {
        String val = licensePlate.getEditText().getText().toString().trim();

        if (val.isEmpty()) {
            licensePlate.setError("Field can not be empty");
            return false;
        } else if (val.length() < 9) {
            licensePlate.setError("Enter valid License Plate number!!");
            return false;
        }else {
            licensePlate.setError(null);
            licensePlate.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword() {
        String val = password.getEditText().getText().toString().trim();
        String checkPassword = "\\A\\w{1,20}\\z";        //BUG HERE

        if (val.isEmpty()) {
            password.setError("Field can not be empty");
            return false;
        } else if (val.length() > 20) {
            username.setError("Password is too large!");
            return false;
        } else if (!val.matches(checkPassword)) {
            password.setError("Password should contain white spaces!");
            return false;
        } else {
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }



}