package com.example.fuelisticv2driver.LoginSignUp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.example.fuelisticv2driver.R;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

public class SignUp2ndScreen extends AppCompatActivity {

    TextInputLayout phoneNumber;
    CountryCodePicker codePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.activity_sign_up2nd_screen);

        //Hooks
        codePicker= findViewById(R.id.country_code_picker);
        phoneNumber= findViewById(R.id.mobile_number);
    }

    public void callLoginScreen(View view){
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }

    public void callOtpVerify(View view){
        if( !validatePhoneNumber() ){
            return;
        }

        // get data passed from prev screen
        String _fullName = getIntent().getStringExtra("fullName");
        String _licensePlate = getIntent().getStringExtra("licensePlate");
        String _username = getIntent().getStringExtra("username");
        String _password = getIntent().getStringExtra("password");
        String _aadhaar = getIntent().getStringExtra("aadhaar");


        //Get complete phone number
        String _getUserEnteredPhoneNumber = phoneNumber.getEditText().getText().toString().trim();
        //Remove first zero if entered!
        if (_getUserEnteredPhoneNumber.charAt(0) == '0') {
            _getUserEnteredPhoneNumber = _getUserEnteredPhoneNumber.substring(1);
        }
        //Complete phone number

        final String _phoneNo = "+" + codePicker.getFullNumber() + _getUserEnteredPhoneNumber;



        Intent intent = new Intent(getApplicationContext(), VerifyOTP.class);
        //pass data to next activity
        intent.putExtra("fullName", _fullName);
        intent.putExtra("licensePlate", _licensePlate);
        intent.putExtra("username", _username);
        intent.putExtra("password", _password);
        intent.putExtra("aadhaar", _aadhaar);
        intent.putExtra("phoneNo", _phoneNo);
        startActivity(intent);
        finish();
    }

    private boolean validatePhoneNumber() {
        String val = phoneNumber.getEditText().getText().toString().trim();
        String checkspaces = "\\A\\w{1,20}\\z";
        if (val.isEmpty()) {
            phoneNumber.setError("Enter valid phone number");
            return false;
        } else if (!val.matches(checkspaces)) {
            phoneNumber.setError("No White spaces are allowed!");
            return false;
        } else {
            phoneNumber.setError(null);
            phoneNumber.setErrorEnabled(false);
            return true;
        }
    }
}