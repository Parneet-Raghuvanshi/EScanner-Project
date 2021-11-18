package com.example.escannrr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    TextView loginhere_txt;
    EditText phone_id_txt,otp_txt;
    Button login_btn,finish_btn;
    private String mVerificationId;
    PhoneAuthProvider.ForceResendingToken token;
    private String code = "91";
    ProgressBar login_pb;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginhere_txt = findViewById(R.id.loginhere_txt);
        phone_id_txt = findViewById(R.id.phone_id_txt);
        login_btn = findViewById(R.id.login_btn);
        login_pb = findViewById(R.id.pb_reg_action);
        finish_btn = findViewById(R.id.finish_btn);
        otp_txt = findViewById(R.id.otp_txt);
        mAuth = FirebaseAuth.getInstance();

        otp_txt.setVisibility(View.GONE);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
//                boolean valid = validateUser();
//                login_btn.setVisibility(View.GONE);
//                login_pb.setVisibility(View.VISIBLE);
//                if (valid){
//                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
//                    databaseReference.keepSynced(true);
//                    databaseReference.orderByChild("number").equalTo(phone_id_txt.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            if (dataSnapshot.getValue() == null){
//                                login_btn.setVisibility(View.VISIBLE);
//                                login_pb.setVisibility(View.GONE);
//                                Toast.makeText(LoginActivity.this, "You don't have Registered yet", Toast.LENGTH_SHORT).show();
//                            }
//                            else {
//                                phone_id_txt.setKeyListener(null);
//                                otp_txt.setVisibility(View.VISIBLE);
//                                login_pb.setVisibility(View.GONE);
//                                login_btn.setVisibility(View.GONE);
//                                finish_btn.setVisibility(View.VISIBLE);
//
//                                final String number=phone_id_txt.getText().toString().trim();
//                                final String mobile = "+"+code+number;
//                                //sendVerificationCode(mobile);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });
//                }
//                else {
//                    login_pb.setVisibility(View.GONE);
//                    login_btn.setVisibility(View.VISIBLE);
//                }
            }
        });

        loginhere_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,Register.class);
                startActivity(intent);
                finish();
            }
        });

        finish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = otp_txt.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    otp_txt.setError("Enter valid code");
                    otp_txt.requestFocus();
                    return;
                }

                login_pb.setVisibility(View.VISIBLE);
                finish_btn.setVisibility(View.GONE);

                //verifying the code entered manually
                verifyVerificationCode(code);
            }
        });
    }

//    private void sendVerificationCode(String mobile) {
//        PhoneAuthProvider.getInstance().verifyPhoneNumber(
//                mobile,
//                120,
//                TimeUnit.SECONDS,
//                TaskExecutors.MAIN_THREAD,
//                mCallbacks);
//    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                otp_txt.setText(code);

                //verifying the code
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            login_pb.setVisibility(View.GONE);
            finish_btn.setVisibility(View.VISIBLE);
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            //storing the verification id that is sent to the user
            mVerificationId = s;
            token=forceResendingToken;

        }
    };

    private void verifyVerificationCode(String code) {
        //creating the credential
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            //mAuth.signOut();
            //signing the user

            signInWithPhoneAuthCredential(credential);

        }catch (Exception e){ //ss
            Toast toast = Toast.makeText(getApplicationContext(), "Verification Code is wrong, try again", Toast.LENGTH_SHORT);
            toast.setGravity( Gravity.CENTER,0,0);
            toast.show();
            login_pb.setVisibility(View.GONE);
            finish_btn.setVisibility(View.VISIBLE);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //verification successful we will start the dashboard.
                    Toast.makeText(LoginActivity.this, "Welcome Back", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    //verification unsuccessful.. display an error message
                    String message = "Somthing is wrong, we will fix it soon...";
                    login_pb.setVisibility(View.GONE);
                    finish_btn.setVisibility(View.VISIBLE);

                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        message = "Invalid code entered...";
                    }
                    Toast.makeText(LoginActivity.this,message, Toast.LENGTH_LONG).show();
                    login_pb.setVisibility(View.GONE);
                    finish_btn.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public boolean validateUser(){
        final String phno = phone_id_txt.getText().toString();

        if (phno.isEmpty()){
            phone_id_txt.setError("Please Enter Your Phone Number");
            phone_id_txt.requestFocus();
            return false;
        }
        else if (phno.length() != 10){
            phone_id_txt.setError("Invalid Phone Number");
            phone_id_txt.requestFocus();
            return false;
        }
        else {
            return true;
        }
    }
}