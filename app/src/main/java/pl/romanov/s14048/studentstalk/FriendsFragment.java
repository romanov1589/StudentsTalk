package pl.romanov.s14048.studentstalk;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView myFriendsList;
    private DatabaseReference firendsReference;
    private DatabaseReference usersReference;
    private FirebaseAuth mAuth;

    String onlineUserId;
    private View myMainView;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        myFriendsList = (RecyclerView) myMainView.findViewById(R.id.friends_list);

        mAuth = FirebaseAuth.getInstance();
        onlineUserId = mAuth.getCurrentUser().getUid();

        firendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(onlineUserId);
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        myFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return myMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                        Friends.class,
                        R.layout.all_users_display_layout,
                        FriendsViewHolder.class,
                        firendsReference

        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                      viewHolder.setDate(model.getDate());

                      final String listUserId = getRef(position).getKey();
                      usersReference.child(listUserId).addValueEventListener(new ValueEventListener() {
                          @Override
                          public void onDataChange(final DataSnapshot dataSnapshot) {
                                final String userName = dataSnapshot.child("user_name").getValue().toString();
                                String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();

                                if(dataSnapshot.hasChild("online")){
                                    String onlineStatus = (String) dataSnapshot.child("online").getValue().toString();
                                    viewHolder.setUserOnline(onlineStatus);
                                }

                                viewHolder.setUserName(userName);
                                viewHolder.setThumbImage(thumbImage, getContext());

                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CharSequence options[] = new CharSequence[]{
                                                userName + "'s Profile",
                                                "Send Message"
                                        };
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Select Options");
                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int posiotion) {
                                                if(posiotion == 0){
                                                    Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                                    profileIntent.putExtra("visit_user_id", listUserId);
                                                    startActivity(profileIntent);
                                                }
                                                if(posiotion == 1){
                                                    if(dataSnapshot.child("online").exists()){
                                                        Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                                        chatIntent.putExtra("visit_user_id", listUserId);
                                                        chatIntent.putExtra("user_name", userName);
                                                        startActivity(chatIntent);
                                                    }else{
                                                        usersReference.child(listUserId).child("online")
                                                                .setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                                                chatIntent.putExtra("visit_user_id", listUserId);
                                                                chatIntent.putExtra("user_name", userName);
                                                                startActivity(chatIntent);
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        });

                                        builder.show();
                                    }
                                });

                          }

                          @Override
                          public void onCancelled(DatabaseError databaseError) {

                          }
                      });

            }
        };
        myFriendsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date){
            TextView sinceFriendsDate = (TextView) mView.findViewById(R.id.all_users_status);
            sinceFriendsDate.setText(date);
        }
        public void setUserName(String userName){
            TextView userNameDisplay = (TextView) mView.findViewById(R.id.all_users_username);
            userNameDisplay.setText(userName);
        }

        public void setThumbImage(String thumbImage, Context ctx) {
            CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(thumbImage).placeholder(R.drawable.default_profile_image).into(thumb_image);
        }

        public void setUserOnline(String onlineStatus) {
            ImageView onlineStatusView = (ImageView) mView.findViewById(R.id.online_status);;
            if(onlineStatus.equals("true")){
                onlineStatusView.setVisibility(View.VISIBLE);
            }else{
                onlineStatusView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
