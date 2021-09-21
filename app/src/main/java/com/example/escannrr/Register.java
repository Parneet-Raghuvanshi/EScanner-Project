package com.example.escannrr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Register extends AppCompatActivity {

    TextView reghere_txt,readmore_txt;
    Button login_btn,finish_btn;
    CheckBox terms_checkbox;
    EditText email_id_txt,name_id_txt,phone_id_text,otp_txt;
    ProgressBar login_pb;
    int code=91;
    private String mVerificationId;
    PhoneAuthProvider.ForceResendingToken token;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        reghere_txt = findViewById(R.id.reghere_txt);
        login_btn = findViewById(R.id.login_btn);
        finish_btn = findViewById(R.id.finish_btn);
        terms_checkbox = findViewById(R.id.terms_checkbox);
        email_id_txt = findViewById(R.id.email_id_txt);
        name_id_txt = findViewById(R.id.name_id_txt);
        phone_id_text = findViewById(R.id.phone_id_txt);
        login_pb = findViewById(R.id.pb_login_action);
        otp_txt = findViewById(R.id.otp_txt);
        mAuth = FirebaseAuth.getInstance();
        readmore_txt = findViewById(R.id.readmore_txt);

        otp_txt.setVisibility(View.GONE);

        reghere_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_btn.setVisibility(View.GONE);
                login_pb.setVisibility(View.VISIBLE);
                boolean valid = validateUser();
                if (valid){
                    final String number=phone_id_text.getText().toString().trim();
                    DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("Users");
                    dbref.keepSynced(true);
                    dbref.orderByChild("number").equalTo(number).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null){
                                login_pb.setVisibility(View.GONE);
                                login_btn.setVisibility(View.VISIBLE);
                                Toast.makeText(Register.this,"User on this phone Number Already Exists",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                otp_txt.setVisibility(View.VISIBLE);
                                terms_checkbox.setKeyListener(null);
                                email_id_txt.setKeyListener(null);
                                name_id_txt.setKeyListener(null);
                                phone_id_text.setKeyListener(null);
                                login_pb.setVisibility(View.GONE);
                                login_btn.setVisibility(View.GONE);
                                finish_btn.setVisibility(View.VISIBLE);

                                final String email = email_id_txt.getText().toString().trim();
                                final String number = phone_id_text.getText().toString().trim();
                                final String name = name_id_txt.getText().toString().trim();
                                final String mobile = "+"+code+number;

                                sendVerificationCode(mobile);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    login_pb.setVisibility(View.GONE);
                    login_btn.setVisibility(View.VISIBLE);
                }
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

        readmore_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(Register.this,Termsandconditions.class);
                startActivity(intent);*/
            }
        });
    }

    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile,
                120,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

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
            Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
        mAuth.signInWithCredential(credential).addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //verification successful we will start the dashboard.
                    saveData();

                } else {
                    //verification unsuccessful.. display an error message
                    String message = "Somthing is wrong, we will fix it soon...";
                    login_pb.setVisibility(View.GONE);
                    finish_btn.setVisibility(View.VISIBLE);

                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        message = "Invalid code entered...";
                    }
                    Toast.makeText(Register.this,message, Toast.LENGTH_LONG).show();
                    login_pb.setVisibility(View.GONE);
                    finish_btn.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private boolean validateUser(){
        final String email = email_id_txt.getText().toString().trim();
        final String number = phone_id_text.getText().toString().trim();
        final String name = name_id_txt.getText().toString().trim();

        if (email.isEmpty()){
            email_id_txt.setError("Email Required");
            email_id_txt.requestFocus();
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            email_id_txt.setError("Enter a valid Email");
            email_id_txt.requestFocus();
            return false;
        }
        else if(number.isEmpty()){
            phone_id_text.setError("Please Enter Your Phone Number");
            phone_id_text.requestFocus();
            return false;
        }
        else if (number.length() != 10) {
            phone_id_text.setError("Invalid Phone Number");
            phone_id_text.requestFocus();
            return false;
        }

        else if(name.isEmpty()){
            name_id_txt.setError("Please Enter Your Name");
            name_id_txt.requestFocus();
            return false;
        }

        else if (!terms_checkbox.isChecked()){
            Toast.makeText(this, "Please Accept T&C.", Toast.LENGTH_SHORT).show();
            return false;
        }

        else {
            return true;
        }
    }

    public void saveData(){
        final String email = email_id_txt.getText().toString().trim();
        final String number = phone_id_text.getText().toString().trim();
        final String name = name_id_txt.getText().toString().trim();

        Calendar calendar = Calendar.getInstance();
        String currentdate = DateFormat.getDateInstance().format(calendar.getTime());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.keepSynced(true);
        databaseReference.child("uid").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.child("email").setValue(email.toLowerCase());
        databaseReference.child("number").setValue(number);
        databaseReference.child("name").setValue(name);
        databaseReference.child("sname").setValue(name.toLowerCase());
        databaseReference.child("regdate").setValue(currentdate);

        Intent intent = new Intent(Register.this,DashBoard.class);
        startActivity(intent);
        finish();
    }
}

