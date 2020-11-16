package com.example.fuelisticv2driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fuelisticv2driver.Common.Common;
import com.example.fuelisticv2driver.LoginSignUp.AppStartUpScreen;
import com.example.fuelisticv2driver.Model.DriverUserModel;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {

    ImageView hangInThere;
    TextView authSoon;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private CompositeDisposable compositeDisposable =  new CompositeDisposable();
    private DatabaseReference sellerRef;
    private List<AuthUI.IdpConfig> providers;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if(listener != null)
            firebaseAuth.removeAuthStateListener(listener);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.activity_main);

        hangInThere = findViewById(R.id.hangInThere);
        authSoon = findViewById(R.id.authSoon);

        init();
    }

    private void init() {

        sellerRef = FirebaseDatabase.getInstance().getReference(Common.DRIVER_REF);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        listener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if(user != null){
                //Already logged in
                checkUserFromFirebase(user);
            }
            else{
                startActivity(new Intent(MainActivity.this, AppStartUpScreen.class));
                finish();
            }
        };
    }

    private void checkUserFromFirebase(FirebaseUser user) {
        dialog.show();
        hangInThere.setVisibility(View.GONE);
        authSoon.setVisibility(View.GONE);
        sellerRef.child(user.getPhoneNumber())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            DriverUserModel sellerUserModel = dataSnapshot.getValue(DriverUserModel.class);
                            if(sellerUserModel.isActive()){
                                gotoHomeActivity(sellerUserModel);
                            }
                            else{
                                Toast.makeText(MainActivity.this, "You are not yet authorised!! Contact your Seller for further information.", Toast.LENGTH_LONG).show();
                                hangInThere.setVisibility(View.VISIBLE);
                                authSoon.setVisibility(View.VISIBLE);
                            }

                        }
                        else {
                            startActivity(new Intent(MainActivity.this, AppStartUpScreen.class));
                            finish();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void gotoHomeActivity(DriverUserModel driverUserModel) {
        dialog.dismiss();
        Common.currentDriverUser = driverUserModel;
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        finish();
    }

}