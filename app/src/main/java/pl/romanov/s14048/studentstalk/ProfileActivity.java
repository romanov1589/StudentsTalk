package pl.romanov.s14048.studentstalk;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private Button sendFriendRequestButton;
    private Button declineFriendRequestButton;
    private TextView profileName;
    private TextView profileStatus;
    private ImageView profileImage;

    private DatabaseReference usersReference;

    private String CURRENT_STATE;
    private DatabaseReference friendRequestReference;
    private FirebaseAuth mAuth;
    String senderUserId;
    String receiverUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();



        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();



        sendFriendRequestButton = (Button) findViewById(R.id.profile_visit_send_req_button);
        declineFriendRequestButton = (Button) findViewById(R.id.profile_visit_decline_friend_req_button);
        profileName = (TextView) findViewById(R.id.profie_visit_username);
        profileStatus = (TextView) findViewById(R.id.profile_visit_user_status);
        profileImage = (ImageView) findViewById(R.id.profile_visit_user_image);

        CURRENT_STATE = "not_friends";

        usersReference.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                profileName.setText(name);
                profileStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_profile_image).into(profileImage);

                friendRequestReference.child(senderUserId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(receiverUserId)){
                                    String reqType = dataSnapshot.child(receiverUserId).child("request_type")
                                            .getValue().toString();
                                    if(reqType.equals("sent")){
                                        CURRENT_STATE = "request_sent";
                                        sendFriendRequestButton.setText("Cancel Friend Request");
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 sendFriendRequestButton.setEnabled(false);

                 if(CURRENT_STATE.equals("not_friends")){
                     sendFriendRequestToPerson();
                 }
                 if(CURRENT_STATE.equals("request_sent")){
                     cancelFriendRequest();
                 }
            }
        });
    }

    private void cancelFriendRequest() {
        friendRequestReference.child(senderUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestReference.child(receiverUserId).child(senderUserId)
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        sendFriendRequestButton.setEnabled(true);
                                        CURRENT_STATE = "not_friends";
                                        sendFriendRequestButton.setText("Send Friend Request");
                                    }
                                }
                            });

                        }
                    }
                });
    }

    private void sendFriendRequestToPerson() {
        friendRequestReference.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    friendRequestReference.child(receiverUserId).child(senderUserId)
                            .child("request_type").setValue("receiver").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendFriendRequestButton.setEnabled(true);
                                CURRENT_STATE = "request_sent";
                                sendFriendRequestButton.setText("Cancel Friend Request");
                            }
                        }
                    });
                }
            }
        });
    }
}
