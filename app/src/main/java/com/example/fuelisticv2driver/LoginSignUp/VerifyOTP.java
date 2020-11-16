package com.example.fuelisticv2driver.LoginSignUp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.example.fuelisticv2driver.Common.Common;
import com.example.fuelisticv2driver.MainActivity;
import com.example.fuelisticv2driver.Model.DriverUserModel;
import com.example.fuelisticv2driver.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

import dmax.dialog.SpotsDialog;

public class VerifyOTP extends AppCompatActivity {

    PinView pinFromUser;
    String fullName, phoneNo, licensePlate, username, password, aadhaar, whatToDO;
    TextView otpDescriptionText;
    String codeBySystem;
    private AlertDialog dialog;
    private DatabaseReference sellerRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.activity_verify_otp);

        //hooks
        pinFromUser = findViewById(R.id.pin_view);
        otpDescriptionText = findViewById(R.id.otp_description_text);

        //Get all the data from Intent
        fullName = getIntent().getStringExtra("fullName");
        licensePlate = getIntent().getStringExtra("licensePlate");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        phoneNo = getIntent().getStringExtra("phoneNo");
        aadhaar = getIntent().getStringExtra("aadhaar");
//        whatToDO = getIntent().getStringExtra("whatToDO");

        otpDescriptionText.setText("Enter One Time Password Sent On " + phoneNo);

        sendVerificationCodeToUser(phoneNo);

    }

    private void sendVerificationCodeToUser(String phoneNo) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNo,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,// Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    String code = phoneAuthCredential.getSmsCode();
                    if (code != null) {
                        pinFromUser.setText(code);
                        verifyCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(VerifyOTP.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    codeBySystem = s;
                }



            };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeBySystem, code);
        signInWithPhoneAuthCredential(credential);
    }

    public void callLoginScreenFromSIgnUp(View view) {
        String code = pinFromUser.getText().toString();
        if (!code.isEmpty()) {
            verifyCode(code);

        }
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        storeNewUserData();

                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(VerifyOTP.this, "Verification Not Completed! Try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void storeNewUserData() {

        sellerRef = FirebaseDatabase.getInstance().getReference(Common.DRIVER_REF);
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        DriverUserModel userModel= new DriverUserModel();
        userModel.setPhoneNo(phoneNo);
        userModel.setFullName(fullName);
        userModel.setUsername(username);
        userModel.setAadhaar(aadhaar);
        userModel.setLicensePlate(licensePlate);
        userModel.setPassword(password);
        userModel.setActive(false);

        dialog.show();

        sellerRef.child(phoneNo).setValue(userModel)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(VerifyOTP.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                dialog.dismiss();
                Toast.makeText(VerifyOTP.this, "Congratulations!! Registration Complete. You will be authorized by your seller soon." , Toast.LENGTH_LONG).show();
                startActivity(new Intent(VerifyOTP.this, MainActivity.class));
                finish();
            }
        });
    }

//    private void gotoHomeActivity(SellerUserModel userModel) {
//        Common.currentSellerUser = userModel;
//        startActivity(new Intent(VerifyOTP.this, HomeActivity.class));
//        finish();
//    }



}