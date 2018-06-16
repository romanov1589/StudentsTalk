package pl.romanov.s14048.studentstalk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        String visitUserId = getIntent().getExtras().get("visit_user_id").toString();
        Toast.makeText(ProfileActivity.this, visitUserId, Toast.LENGTH_SHORT).show();
    }
}
