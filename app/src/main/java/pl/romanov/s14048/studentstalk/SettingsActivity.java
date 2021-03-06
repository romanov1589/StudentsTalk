package pl.romanov.s14048.studentstalk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView settingsDispayProfileImage;
    private TextView settingsDisplayName;
    private TextView settingsDisplayStatus;
    private Button settingsChangeProfileImageButton;
    private Button settingsChangeStatusButton;

    private final static int galleryPick = 1;

    private StorageReference storeProfileImageStorageRef;

    private DatabaseReference getUserDataReference;
    private FirebaseAuth mAuth;

    Bitmap thumbBitmap = null;
    private StorageReference thumbImageRef;

    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        String onlineUserId = mAuth.getCurrentUser().getUid();

        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(onlineUserId);

        storeProfileImageStorageRef = FirebaseStorage.getInstance().getReference().child("Profile_Images");
        thumbImageRef = FirebaseStorage.getInstance().getReference().child("Thumb_Images");

        settingsDispayProfileImage = (CircleImageView) findViewById(R.id.settings_profile_image);
        settingsDisplayName = (TextView) findViewById(R.id.settings_username);
        settingsDisplayStatus = (TextView) findViewById(R.id.settings_user_status);
        settingsChangeProfileImageButton = (Button) findViewById(R.id.settings_change_profile_image_button);
        settingsChangeStatusButton = (Button) findViewById(R.id.settings_change_profile_status);

        loadingBar = new ProgressDialog(this);

        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                //final for offline
                String image = dataSnapshot.child("user_image").getValue().toString();
                String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();

                settingsDisplayName.setText(name);
                settingsDisplayStatus.setText(status);

                if (!image.equals("default_profile")) {

                    Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_profile_image)
                            .into(settingsDispayProfileImage);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        settingsChangeProfileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPick);
            }
        });

        settingsChangeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String oldStatus = settingsDisplayStatus.getText().toString();

                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("user_status", oldStatus);
                startActivity(statusIntent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Updating Profile Image");
                loadingBar.setMessage("Please wait...");
                loadingBar.show();

                Uri resultUri = result.getUri();

                File thumbFilePathUri = new File(resultUri.getPath());
                try {
                    thumbBitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(thumbFilePathUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                final byte[] thumbByte = byteArrayOutputStream.toByteArray();


                String userId = mAuth.getCurrentUser().getUid();

                StorageReference filePath = storeProfileImageStorageRef.child(userId + ".jpg");
                final StorageReference thumbFilePath = thumbImageRef.child(userId + ".jpg");


                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Saving your profile Image to Firebase Storage...",
                                    Toast.LENGTH_LONG).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumbFilePath.putBytes(thumbByte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbTask) {
                                    String thumbDownloadUrl = thumbTask.getResult().getDownloadUrl().toString();
                                    if (thumbTask.isSuccessful()) {
                                        Map updateUserData = new HashMap();
                                        updateUserData.put("user_image", downloadUrl);
                                        updateUserData.put("user_thumb_image", thumbDownloadUrl);

                                        getUserDataReference.updateChildren(updateUserData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(SettingsActivity.this, "Image updated Successfully!",
                                                        Toast.LENGTH_LONG).show();
                                                loadingBar.dismiss();
                                            }
                                        });

                                    }
                                }
                            });
                        } else {
                            Toast.makeText(SettingsActivity.this, "Error ocured, while uploading your Profile Pic",
                                    Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
