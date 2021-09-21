package com.example.escannrr;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class UploadImageact extends AppCompatActivity {

    Button choose_btn,done_btn;
    CircleImageView user_img;
    ProgressBar progressBar;
    private static final int ImageBack = 1;
    StorageReference storageReference;

    @Override
    protected void onStart() {
        super.onStart();

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Usres");
        databaseReference.keepSynced(true);
        databaseReference.orderByChild("uid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    User_md user_md = dataSnapshot1.getValue(User_md.class);

                    if (user_md.getImguri()!= null){
                        Picasso.get().load(user_md.getImguri()).into(user_img);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_imageact);

        storageReference = FirebaseStorage.getInstance().getReference().child("ProfileImages");

        choose_btn = findViewById(R.id.update_btn);
        user_img = findViewById(R.id.image_user);
        progressBar = findViewById(R.id.update_action);
        progressBar.setVisibility(View.GONE);
        done_btn = findViewById(R.id.done_btn);
        done_btn.setVisibility(View.GONE);

        choose_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,ImageBack);
            }
        });

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Usres");
        databaseReference.keepSynced(true);
        databaseReference.orderByChild("uid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    User_md user_md = dataSnapshot1.getValue(User_md.class);

                    if (user_md.getImguri()!= null){
                        Picasso.get().load(user_md.getImguri()).into(user_img);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        done_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ImageBack){

            if (resultCode == RESULT_OK){

                progressBar.setVisibility(View.VISIBLE);
                choose_btn.setVisibility(View.GONE);

                Uri user_img_uri = data.getData();

                final StorageReference ImageName = storageReference.child(user_img_uri.getLastPathSegment());

                ImageName.putFile(user_img_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        ImageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
                                databaseReference.keepSynced(true);
                                databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("imguri").setValue(String.valueOf(uri)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(UploadImageact.this,"Profile Image Updated",Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                        choose_btn.setVisibility(View.GONE);
                                        done_btn.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        });

                    }
                });

            }
            else {
                progressBar.setVisibility(View.GONE);
                choose_btn.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            }

        }
        else {
            progressBar.setVisibility(View.GONE);
            choose_btn.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
        }
    }
}
