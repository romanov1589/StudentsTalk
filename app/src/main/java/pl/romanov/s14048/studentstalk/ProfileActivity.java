package pl.romanov.s14048.studentstalk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private Button sendFriendRequest;
    private Button declineFriendRequest;
    private TextView profileName;
    private TextView profileStatus;
    private ImageView profileImage;

    private DatabaseReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        String visitUserId = getIntent().getExtras().get("visit_user_id").toString();

        sendFriendRequest = (Button) findViewById(R.id.profile_visit_send_req_button);
        declineFriendRequest = (Button) findViewById(R.id.profile_visit_decline_friend_req_button);
        profileName = (TextView) findViewById(R.id.profie_visit_username);
        profileStatus = (TextView) findViewById(R.id.profile_visit_user_status);
        profileImage = (ImageView) findViewById(R.id.profile_visit_user_image);

        usersReference.child(visitUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                profileName.setText(name);
                profileStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_profile_image).into(profileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
