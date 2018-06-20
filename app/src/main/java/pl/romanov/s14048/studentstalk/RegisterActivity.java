package pl.romanov.s14048.studentstalk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference storeUserDefaultDataReference;

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private EditText registerUserName;
    private EditText registerUserEmail;
    private EditText registerUserPassword;
    private Button createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        registerUserName = (EditText) findViewById(R.id.register_name);
        registerUserEmail = (EditText) findViewById(R.id.register_email);
        registerUserPassword = (EditText) findViewById(R.id.register_password);
        createAccountButton = (Button) findViewById(R.id.create_account_button);
        loadingBar = new ProgressDialog(this);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = registerUserName.getText().toString();
                String email = registerUserEmail.getText().toString();
                String password = registerUserPassword.getText().toString();

                RegisterAccount(name, email, password);
            }
        });
    }

    private void RegisterAccount(final String name, String email, String password) {
        if(TextUtils.isEmpty(name)){
            Toast.makeText(RegisterActivity.this, "Please write your name", Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(email)){
            Toast.makeText(RegisterActivity.this, "Please write your email", Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(RegisterActivity.this, "Please write your password", Toast.LENGTH_LONG).show();
        }

        else{
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we are creating account");
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        String currentUserId = mAuth.getCurrentUser().getUid();
                        storeUserDefaultDataReference = FirebaseDatabase.getInstance().
                                getReference().child("Users").child(currentUserId);

                        storeUserDefaultDataReference.child("user_name").setValue(name);
                        storeUserDefaultDataReference.child("user_status").setValue("Please write your students id here (sXXXXX)");
                        storeUserDefaultDataReference.child("user_image").setValue("default_profile");
                        storeUserDefaultDataReference.child("device_token").setValue(deviceToken);
                        storeUserDefaultDataReference.child("user_thumb_image").setValue("default_image")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(mainIntent);
                                            finish();
                                        }
                                    }
                                });
                    }
                    else{
                        Toast.makeText(RegisterActivity.this, "Error Occured, Try Again...",
                                Toast.LENGTH_LONG).show();
                    }
                    loadingBar.dismiss();
                }
            });
        }
    }

}
