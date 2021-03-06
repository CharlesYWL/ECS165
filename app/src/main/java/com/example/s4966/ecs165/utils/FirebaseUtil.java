package com.example.s4966.ecs165.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.s4966.ecs165.MainActivity;
import com.example.s4966.ecs165.R;
import com.example.s4966.ecs165.models.CommentModel;
import com.example.s4966.ecs165.models.PostTracer;
import com.example.s4966.ecs165.models.Postmodel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseUtil {
    private static final String TAG = "FirebaseUtil";

    //firebase
    private Context context;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private String userID;
    private double photoUploadProgress = 0;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public void setAuth(FirebaseAuth auth) {
        this.auth = auth;
    }

    public FirebaseDatabase getDatabase() {
        return database;
    }

    public void setDatabase(FirebaseDatabase database) {
        this.database = database;
    }

    public DatabaseReference getDatabaseRef() {
        return databaseRef;
    }

    public void setDatabaseRef(DatabaseReference databaseRef) {
        this.databaseRef = databaseRef;
    }

    public StorageReference getStorageRef() {
        return storageRef;
    }

    public void setStorageRef(StorageReference storageRef) {
        this.storageRef = storageRef;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    //vars
    //private Context mContext;
    //private double mPhotoUploadProgress = 0;

    public ArrayList<String> getHashTags(String text){
        ArrayList<String> tags = new ArrayList<>();
        String[] segments = text.split("#");
        if(segments.length == 1){
            return tags;
        }
        for(int i = 1; i < segments.length; i++){
            String tag = getFirstWord(segments[i]);
            tags.add(tag);
        }
        return tags;
    }

    private String getFirstWord(String s){
        String result = s.split(" ")[0];
        return result.toLowerCase();
    }

    public FirebaseUtil(Context context){
        this.context = context;
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        if(auth.getCurrentUser() != null){
            userID = auth.getCurrentUser().getUid();
        }
    }

    public void uploadNewPost(final String text, Drawable pic){
        Log.d(TAG, "upload new post");

        final String timestamp = getTimestamp();
        StorageReference storageNode = this.storageRef.
                child(FirebasePaths.FIREBASE_POSTIMAGE_STORAGE_PATH + "/" + this.userID + "/" + timestamp);
        Bitmap bm = ((BitmapDrawable) pic).getBitmap();
        byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);
        UploadTask uploadTask = storageNode.putBytes(bytes);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri firebaseUrl = taskSnapshot.getUploadSessionUri();

                Toast.makeText(context, "post upload success", Toast.LENGTH_SHORT).show();

                //add the new photo to 'photos' node and 'user_photos' node
                uploadPostInfoToDatabase(timestamp, text, firebaseUrl.toString());

                //navigate to the main feed so the user can see their photo
                //Intent intent = new Intent(context, MainActivity.class);
                //context.startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "onFailure: Photo upload failed.");
                Toast.makeText(context, "Photo upload failed ", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                if(progress - 15 > photoUploadProgress){
                    Toast.makeText(context, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                    photoUploadProgress = progress;
                }

                Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
            }
        });

    }


    public void addLikeToPost(Postmodel post, final FeedListAdapter adapter){
        String currentUserId = auth.getCurrentUser().getUid();
        databaseRef.child(FirebasePaths.FIREBASE_POST_DATABASE_PATH)
                .child(post.getUser_id())
                .child(post.getPost_id())
                .child(context.getString(R.string.field_like_node))
                .child(currentUserId)
                .setValue(currentUserId).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void removeLikeToPost(Postmodel post, final FeedListAdapter adapter){
        String currentUserId = auth.getCurrentUser().getUid();
        databaseRef.child(FirebasePaths.FIREBASE_POST_DATABASE_PATH)
                .child(post.getUser_id())
                .child(post.getPost_id())
                .child(context.getString(R.string.field_like_node))
                .child(currentUserId)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void addCommandToPost(String pid, String uid, CommentModel comment){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        comment.setPid(databaseRef.child(FirebasePaths.FIREBASE_POST_DATABASE_PATH)
                .child(uid).child(pid).child("comments")
                .push().getKey());
        databaseRef.child(FirebasePaths.FIREBASE_POST_DATABASE_PATH)
                .child(uid).child(pid).child("comments").child(comment.getPid())
                .setValue(comment.toMap());
        Toast.makeText(context, "Comment sent!", Toast.LENGTH_SHORT).show();
        //hide keyboard
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void uploadPostInfoToDatabase(String timestamp, String text, String url){
        Log.d(TAG, "addPhotoToDatabase: adding photo to database.");

        //String tags = StringManipulation.getTags(caption);
        String newPostKey = databaseRef.child(FirebasePaths.FIREBASE_POST_DATABASE_PATH).push().getKey();
        Postmodel post = new Postmodel();
        post.setText(text);
        post.setDate_created(timestamp);
        post.setImage_path(url);
        //post.setTags(tags);
        post.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        post.setPost_id(newPostKey);

        //insert into database
        databaseRef.child(FirebasePaths.FIREBASE_POST_DATABASE_PATH)
                .child(post.getUser_id())
                .child(newPostKey)
                .setValue(post);
        //databaseRef.child(FirebasePaths.FIREBASE_POST_DATABASE_PATH).child(newPostKey).setValue(post);

        // gather hashtag info
        PostTracer tracer = new PostTracer(post);
        ArrayList<String> tags = getHashTags(post.getText());
        for(String tag : tags){
            databaseRef.child(FirebasePaths.FIREBASE_HASHTAG_PATH)
                    .child(tag)
                    .push()
                    .setValue(tracer);
        }
    }

    // timestamp used as post image id
    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        return sdf.format(new Date());
    }



}

class ImageManager{
    /**
     * return byte array from a bitmap
     * quality is greater than 0 but less than 100
     * @param bm bitmap
     * @param quality quality ratio out of 100
     * @return
     */
    public static byte[] getBytesFromBitmap(Bitmap bm, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }
}
