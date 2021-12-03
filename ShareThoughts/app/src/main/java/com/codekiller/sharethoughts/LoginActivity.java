package com.codekiller.sharethoughts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codekiller.sharethoughts.UI.UIUtils;
import com.codekiller.sharethoughts.firebase.FBUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class LoginActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText mailText, passText;
    Button loginBtn, cancelBtn;
    TextView forgotView, registerView;
    FBUtils fbUtils;
    UIUtils uiUtils;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mailText = findViewById(R.id.mailid_text);
        passText = findViewById(R.id.password_text);
        loginBtn = findViewById(R.id.login_btn);
        cancelBtn = findViewById(R.id.cancel_btn);
        forgotView = findViewById(R.id.forgot_password_btn);
        registerView = findViewById(R.id.register_btn);

        fbUtils = new FBUtils();
        uiUtils = new UIUtils(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mailText.setText("");
                passText.setText("");
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( mailText.getText().toString().length() != 0 &&  passText.getText().toString().length() != 0 ){
                    if( passText.getText().toString().length() >= 8 ){
                        login(mailText.getText().toString(),passText.getText().toString());
                    }else{
                        toast("password must be above 8 characters");
                        passText.setText("");
                    }
                }else{
                    toast("fill all the fields");
                }
            }
        });

        registerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegistrationActivity.class));
            }
        });
    }

    private void login(String text, String text1) {
        uiUtils.showProgress("Logging in","Please Wait !");
        fbUtils.getAuth().signInWithEmailAndPassword(text,text1)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if( task.isSuccessful() ){
                            uiUtils.progressDismiss();
                            startActivity(new Intent(LoginActivity.this,MainActivity.class));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uiUtils.progressDismiss();
                        toast("check your password or\ncheck your internet");
                    }
                });
    }
    public void toast(String s){
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if( fbUtils.getUser() != null ){
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            finish();
        }
    }
}