package com.example.s4966.ecs165;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.example.s4966.ecs165.models.LikeModel;
import com.example.s4966.ecs165.models.Postmodel;
import com.example.s4966.ecs165.models.User;
import com.example.s4966.ecs165.utils.FirebasePaths;
import com.example.s4966.ecs165.utils.ProfilePostsAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;


public class Profile extends AppCompatActivity {

    private Toolbar mToolbar;
    //need to use for every class
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return true;
    }

    private ImageButton SignoutButton;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private StorageReference storageReference;
    private TextView nameTextView;
    private TextView genderTextView;
    private TextView bioTextView;
    private ImageView imageView;
    private GridView gridView;
    private ArrayList<Postmodel> posts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //toolbar apply to all
        mToolbar=findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setBackWork(mToolbar);

        //init all thing
         mDatabase = FirebaseDatabase.getInstance().getReference();
         user = FirebaseAuth.getInstance().getCurrentUser();
         storageReference = FirebaseStorage.getInstance().getReference();
         nameTextView = findViewById(R.id.name_textView);
         genderTextView = findViewById(R.id.gender_textView);
         bioTextView= findViewById(R.id.bio_textView);
         imageView = findViewById(R.id.pic_imageview);
         //gridView = findViewById(R.id.profile_grid_view);

         if(user == null) { // for some rare case
             Intent intent = new Intent().setClass(Profile.this,LoginActivity.class);
             startActivity(intent);
         }


        //[command start]
         mDatabase.child("users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = (String) dataSnapshot.child("username").getValue();
                String bio = (String) dataSnapshot.child("bio").getValue();
                String email = (String) dataSnapshot.child("email").getValue();
                User.GENDER gender = User.GENDER.UNKNOWN;
                switch (dataSnapshot.child("gender").getValue().toString()){
                    case "MALE":
                        gender = User.GENDER.MALE;
                        break;
                    case "FEMALE":
                        gender = User.GENDER.FEMALE;
                        break;
                    case "UNKNOWN":
                        gender = User.GENDER.UNKNOWN;
                        break;
                }
                final User us = new User(user.getUid(),username, bio, email, gender, null);

                if (dataSnapshot.child("pictureId").exists()) {
                    final Semaphore semaphore = new Semaphore(1);
                    final String pictureId = (String) dataSnapshot.child("pictureId").getValue();
                    // TODO there is a hard image size limit, may fix it in future.
                    final long TEN_MEGABYTE = 10 * 1024 * 1024;
                    StorageReference storagePicNode = storageReference.child("pic");
                    storagePicNode.child(pictureId).getBytes(TEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            imageView.setImageBitmap(bmp);
                        }
                    });
                }
                us.setUid(user.getUid());
                updataUI(us);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //[command end]

        //this one listen to toolbar click
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.Setting:
                        //open modify activity
                        Intent intent = new Intent();
                        intent.setClass(Profile.this,ProfileModify.class);
                        startActivity(intent);

                        return true;
                }
                return false;
            }
        });
        //make sure it has user

        // set up grid view
        //setupPostGrid();
    }


    //make back Navi on tool bar works and direction
    public void setBackWork(Toolbar tb){
        getSupportActionBar().setTitle("Profile");
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });
    }


    public void updataUI(User user){
        //User user = new User();
        //User.getUserFromFireBase(user.getUid(),user);
        nameTextView.setText(user.getUsername());
        bioTextView.setText(user.getBio());
        String g="\0";
        switch (user.getGender()){
            case MALE:
                g = "MALE";
                break;
            case FEMALE:
                g = "FEMALE";
                break;
            case UNKNOWN:
                g = "UNKNOWN";
                break;
        }
        genderTextView.setText(g);
        //TODO need dealwith photo
        imageView.setImageDrawable(user.getPicture());
//        GlideApp.with(this).load(storageReference.child("pic").getDownloadUrl()).into(imageView);

    }

    //change back behavior
    @Override
    public void onBackPressed(){
        startActivity(new Intent(Profile.this,MainActivity.class));
    }

    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();

    }

    public void openMyFeed(View view) {
        FirebaseUser cu = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = new Intent(getApplicationContext(),ShowPosts.class);
        intent.putExtra("uid",cu.getUid().toString());
        startActivity(intent);
    }
}

