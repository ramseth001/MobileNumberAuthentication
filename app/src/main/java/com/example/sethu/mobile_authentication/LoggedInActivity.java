package com.example.sethu.mobile_authentication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoggedInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        String user = (String) getIntent().getExtras().get("userObject");
        Boolean userStatus = (Boolean) getIntent().getExtras().get("userStatus");
        TextView textView = (TextView) findViewById(R.id.textView2);
        String welcome = userStatus ? "Welcome New User(": "Welcome Back (";
        textView.setText(welcome+user+")");
    }

    /**
     * Logging out from the activity
     */
    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Disabling back button
     */
    public void onBackPressed(){

    }
}
