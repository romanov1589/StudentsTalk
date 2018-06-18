package pl.romanov.s14048.studentstalk;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

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

    private DatabaseReference friendsReference;

    private DatabaseReference notificationsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");

//        offline
//        friendRequestReference.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();

        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");

        //        offline
//        friendRequestReference.keepSynced(true);

        //notificationsReference = FirebaseDatabase.getInstance().getReference().child("Notifications");

        //offline
        //notificationsReference.keepSynced(true);




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

                                          declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                          declineFriendRequestButton.setEnabled(false);



                                      }else if(reqType.equals("received")){
                                          CURRENT_STATE = "request_received";
                                          sendFriendRequestButton.setText("Accept Friend Request");

                                          declineFriendRequestButton.setVisibility(View.VISIBLE);
                                          declineFriendRequestButton.setEnabled(true);

                                          declineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View view) {
                                                  declineFriendRequest();
                                              }
                                          });
                                      }
                                  }

                              else{
                                  friendsReference.child(senderUserId)
                                          .addListenerForSingleValueEvent(new ValueEventListener() {
                                              @Override
                                              public void onDataChange(DataSnapshot dataSnapshot) {
                                                  if(dataSnapshot.hasChild(receiverUserId)){
                                                      CURRENT_STATE = "friends";
                                                      sendFriendRequestButton.setText("Unfriend");

                                                      declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                      declineFriendRequestButton.setEnabled(false);
                                                  }
                                              }

                                              @Override
                                              public void onCancelled(DatabaseError databaseError) {

                                              }
                                          });
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

        declineFriendRequestButton.setVisibility(View.INVISIBLE);
        declineFriendRequestButton.setEnabled(false);

        if(!senderUserId.equals(receiverUserId)){
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
                    if(CURRENT_STATE.equals("request_received")){
                        acceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("friends")){
                        unFriendAFriend();
                    }
                }
            });
            }else {
                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                sendFriendRequestButton.setVisibility(View.INVISIBLE);
        }

    }

    private void declineFriendRequest() {
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

                                        declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                        declineFriendRequestButton.setEnabled(false);
                                    }
                                }
                            });

                        }
                    }
                });
    }

    private void unFriendAFriend() {
        friendsReference.child(senderUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendsReference.child(receiverUserId).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendFriendRequestButton.setText("Send Friend Request");

                                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                declineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendRequest() {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String saveCurrentDate = currentDate.format(calForDate.getTime());

        friendsReference.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        friendsReference.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
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
                                                                        CURRENT_STATE = "friends";
                                                                        sendFriendRequestButton.setText("Unfriend");

                                                                        declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                        declineFriendRequestButton.setEnabled(false);
                                                                    }
                                                                }
                                                            });

                                                        }
                                                    }
                                                });
                                    }
                                });
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
                                        declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                        declineFriendRequestButton.setEnabled(false);
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
                            .child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

//                                HashMap<String, String> notificationsData = new HashMap<>();
//                                notificationsData.put("from", senderUserId);
//                                notificationsData.put("type", "request");

//                                notificationsReference.child(receiverUserId).push().setValue(notificationsData)
//                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                if(task.isSuccessful()){
                                                    sendFriendRequestButton.setEnabled(true);
                                                    CURRENT_STATE = "request_sent";
                                                    sendFriendRequestButton.setText("Cancel Friend Request");

                                                    declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                    declineFriendRequestButton.setEnabled(false);
                                                }
//                                            }
//                                        });
//                            }
                        }
                    });
                }
            }
        });
    }
}
