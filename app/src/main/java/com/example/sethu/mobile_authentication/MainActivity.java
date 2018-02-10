package com.example.sethu.mobile_authentication;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

// source: https://firebase.google.com/docs/auth/android/phone-auth

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PhoneAuthActivity";
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private Boolean isNew = false;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    EditText mobileNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mobileNumber = (EditText) findViewById(R.id.editText);
        mobileNumber.setSelection(mobileNumber.getText().length());

        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                requestVerificationCode(mVerificationId, mResendToken);
                // ...
            }
        };


    }

    private void requestVerificationCode(final String verificationId, final PhoneAuthProvider.ForceResendingToken mResendToken) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter verification code");
        final EditText code_input = new EditText(this);
        code_input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(code_input);

        builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code_input.getText().toString());
                signInWithPhoneAuthCredential(credential);
            }
        });

        builder.setNegativeButton("Abort", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Process aborted", Toast.LENGTH_SHORT).show();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            checkingForUserExistence(task);
                            Toast.makeText(MainActivity.this, "Success login", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = task.getResult().getUser();
                            performingSignInProcess(user);
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void checkingForUserExistence(Task<AuthResult> task) {
        isNew = task.getResult().getAdditionalUserInfo().isNewUser();
    }

    private void performingSignInProcess(FirebaseUser user) {

        // Toast.makeText(this, user.getPhoneNumber(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoggedInActivity.class);
        intent.putExtra("userObject", user.getPhoneNumber());
        intent.putExtra("userStatus", isNew);
        startActivity(intent);
    }

    public void onStart() {

        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String temp = (currentUser == null) ? "New SignIn" : currentUser.getPhoneNumber();
        if (currentUser != null) {
            Intent intent = new Intent(this, LoggedInActivity.class);
            intent.putExtra("userObject", temp);
            intent.putExtra("userStatus", false);
            startActivity(intent);
        } else {
            Toast.makeText(this, temp, Toast.LENGTH_SHORT).show();
        }

    }

    public void verifyNumber(View view) {
        //Toast.makeText(this, "lol", Toast.LENGTH_SHORT).show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(mobileNumber.getText().toString(), 60, TimeUnit.SECONDS, this, mCallbacks);
        //PhoneAuthProvider.getInstance().verifyPhoneNumber("+61415130037", 60, TimeUnit.SECONDS, this, mCallbacks);
    }

    public void onBackPressed(){

    }

}
