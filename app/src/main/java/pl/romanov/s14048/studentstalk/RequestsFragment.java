package pl.romanov.s14048.studentstalk;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView myRequestsList;
    private View myMainView;

    private DatabaseReference friendsRequestsReference;
    private FirebaseAuth mAuth;
    String onlineUserId;

    private DatabaseReference usersReference;

    private DatabaseReference friendsDatabaseRef;
    private DatabaseReference friendsReqDatabaseReq;





    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myMainView = inflater.inflate(R.layout.fragment_requests, container, false);
        myRequestsList = (RecyclerView) myMainView.findViewById(R.id.requests_list);
        mAuth = FirebaseAuth.getInstance();
        onlineUserId = mAuth.getCurrentUser().getUid();



        friendsRequestsReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(onlineUserId);
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        friendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendsReqDatabaseReq = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");


        myRequestsList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        myRequestsList.setLayoutManager(linearLayoutManager);






        return myMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests, RequestViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Requests, RequestViewHolder>(
                Requests.class,
                R.layout.friend_request_all_user_layout,
                RequestsFragment.RequestViewHolder.class,
                friendsRequestsReference

        ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Requests model, int position) {

                final String listUsersId = getRef(position).getKey();
                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String requestType = dataSnapshot.getValue().toString();
                            if(requestType.equals("received")){
                                TextView textView = viewHolder.mView.findViewById(R.id.reqest_text_view);
                                textView.setText("Please accept or decline request");
                                usersReference.child(listUsersId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                                        final String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                                        final String userStatus = dataSnapshot.child("user_status").getValue().toString();

                                        viewHolder.setUserName(userName);
                                        viewHolder.setThumbUserImage(thumbImage, getContext());
                                        viewHolder.setUserStatus(userStatus);

                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                CharSequence options[] = new CharSequence[]{
                                                        "Accept Friend Request",
                                                        "Cancel Friend Request"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Friend Request Options");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int posiotion) {
                                                        if(posiotion == 0){
                                                            Calendar calForDate = Calendar.getInstance();
                                                            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                                                            final String saveCurrentDate = currentDate.format(calForDate.getTime());

                                                            friendsDatabaseRef.child(onlineUserId).child(listUsersId).child("date").setValue(saveCurrentDate)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            friendsDatabaseRef.child(listUsersId).child(onlineUserId).child("date").setValue(saveCurrentDate)
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            friendsReqDatabaseReq.child(onlineUserId).child(listUsersId).removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                friendsReqDatabaseReq.child(listUsersId).child(onlineUserId)
                                                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        if(task.isSuccessful()){
                                                                                                                            Toast.makeText(getContext(), "Friend request accepted",
                                                                                                                                    Toast.LENGTH_SHORT).show();
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
                                                        if(posiotion == 1){
                                                            friendsReqDatabaseReq.child(onlineUserId).child(listUsersId).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                friendsReqDatabaseReq.child(listUsersId).child(onlineUserId)
                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if(task.isSuccessful()){
                                                                                            Toast.makeText(getContext(), "Friend request cancelled", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });

                                                                            }
                                                                        }
                                                                    });
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
                            else if(requestType.equals("sent")){
//                                Button reqSentBtn = viewHolder.mView.findViewById(R.id.request_accept_btn);
//                                reqSentBtn.setText("Req Sent");
//                                viewHolder.mView.findViewById(R.id.request_decline_btn).setVisibility(View.INVISIBLE);
                                    TextView textView = viewHolder.mView.findViewById(R.id.reqest_text_view);
                                    textView.setText("Request sent");

                                usersReference.child(listUsersId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                                        final String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                                        final String userStatus = dataSnapshot.child("user_status").getValue().toString();

                                        viewHolder.setUserName(userName);
                                        viewHolder.setThumbUserImage(thumbImage, getContext());
                                        viewHolder.setUserStatus(userStatus);

                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CharSequence options[] = new CharSequence[]{
                                                        "Cancel Friend Request"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Friend Request Sent");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int posiotion) {
                                                        if(posiotion == 0){
                                                            friendsReqDatabaseReq.child(onlineUserId).child(listUsersId).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                friendsReqDatabaseReq.child(listUsersId).child(onlineUserId)
                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if(task.isSuccessful()){
                                                                                            Toast.makeText(getContext(), "Friend request cancelled", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });

                                                                            }
                                                                        }
                                                                    });
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
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        myRequestsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public RequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUserName(String userName) {
            TextView userNameDisplay = (TextView) mView.findViewById(R.id.request_profile_name);
            userNameDisplay.setText(userName);
        }

        public void setThumbUserImage(String thumbImage, Context ctx) {
            CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.request_profile_image);
            Picasso.with(ctx).load(thumbImage).placeholder(R.drawable.default_profile_image).into(thumb_image);
        }

        public void setUserStatus(String userStatus) {
            TextView status = (TextView) mView.findViewById(R.id.request_proffile_status);
            status.setText(userStatus);
        }
    }

}
