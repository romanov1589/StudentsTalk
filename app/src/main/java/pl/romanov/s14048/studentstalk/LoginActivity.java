package pl.romanov.s14048.studentstalk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private FirebaseAuth mAuth;

    private Button logInButton;
    private EditText loginEmail;
    private EditText loginPassword;

    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mToolBar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Sign in");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        logInButton = (Button) findViewById(R.id.login_button);
        loginEmail = (EditText) findViewById(R.id.login_email);
        loginPassword = (EditText) findViewById(R.id.login_password);
        loadingBar = new ProgressDialog(this);

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = loginEmail.getText().toString();
                String password = loginPassword.getText().toString();

                loginUserAccount(email, password);
            }
        });
    }

    private void loginUserAccount(String email, String password) {
        if(TextUtils.isEmpty(email)){
            Toast.makeText(LoginActivity.this, "Please write your email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(LoginActivity.this, "Please write your password", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Login Account");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainIntent);
                        finish();
                    }
                    else{
                        Toast.makeText(LoginActivity.this, "Wrong email or password. " +
                                        "Please check your email and password", Toast.LENGTH_SHORT).show();
                    }
                    loadingBar.dismiss();
                }
            });
        }
    }
}
