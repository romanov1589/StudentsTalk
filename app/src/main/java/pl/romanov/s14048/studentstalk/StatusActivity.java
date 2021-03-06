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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button saveChangesButton;
    private EditText statusInput;
    private DatabaseReference changeStatusReference;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        changeStatusReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        saveChangesButton = (Button) findViewById(R.id.save_status_changes_button);
        statusInput = (EditText) findViewById(R.id.status_input);
        loadingBar = new ProgressDialog(this);

        String oldStatus = getIntent().getExtras().get("user_status").toString();
        statusInput.setText(oldStatus);

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newStatus = statusInput.getText().toString();
                changeProfileStatus(newStatus);
            }
        });
    }

    private void changeProfileStatus(String newStatus) {
        if(TextUtils.isEmpty(newStatus)){
            Toast.makeText(StatusActivity.this, "Please write your status",
                    Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Change profile status");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            changeStatusReference.child("user_status").setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        loadingBar.dismiss();
                        Intent settingsIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                        startActivity(settingsIntent);

                        Toast.makeText(StatusActivity.this, "Profile status updated Successfully!",
                                Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(StatusActivity.this, "Error Occurred...",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
