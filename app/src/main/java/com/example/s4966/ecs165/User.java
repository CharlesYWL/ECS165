package com.example.s4966.ecs165;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.*;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class User {
    public enum GENDER {MALE, FEMALE}

    private String uid;
    private String username;
    private String bio;
    private String email;
    private GENDER gender;
    private Drawable picture;
    private String TAG = "User class";
    /**
     *  pictureId should always be the same with uid, if exists a picture;
     */
    private String pictureId;

    /**
     * Constructor for the User class.
     * @param usernameStr username string
     * @param bioStr bio info string
     * @param emailStr email string
     * @param gen gender
     * @param pic: If there is no picture for the user, please pass null
     */
    public User(String usernameStr, String bioStr, String emailStr, GENDER gen, Drawable pic){
        uid = "";
        username = usernameStr;
        bio = bioStr;
        email = emailStr;
        gender = gen;
        picture = pic;
        pictureId = null;
    }

    /**
     *
     * @param id user id
     * @param usernameStr username string
     * @param bioStr bio info string
     * @param emailStr email string
     * @param gen gender
     * @param pic picture drawable class, if not exist, please specify null.
     */
    public User(String id, String usernameStr, String bioStr, String emailStr, GENDER gen, Drawable pic){
        uid = id;
        username = usernameStr;
        bio = bioStr;
        email = emailStr;
        gender = gen;
        picture = pic;
        pictureId = null;
    }


    public String getUsername(){
        return username;
    }


    public static void addFollow(DatabaseReference databaseFollowsNode, User follow, User followed){
        String TAG = "void addFollow(User follow, User followed)";
        if (!follow.hasUID() || !followed.hasUID()){
            Log.e(TAG, "follow or followed user has no uid,");
            System.exit(1);
        }
        Map<String, Object> result = new HashMap<>();
        // should be foreign key
        result.put("follow", follow.getUid());
        result.put("follow username", follow.getUsername());
        result.put("followed", followed.getUsername());
        databaseFollowsNode.push().setValue(result);
    }

    // TODO: later we need to delete related picture in database as well
    public static void deleteUser(DatabaseReference databaseUserNode, User user){
        databaseUserNode.child(user.getUid()).removeValue();
    }

    public static void addUser(DatabaseReference databaseUserNode, StorageReference storagePicNode, User user){
        if (!user.hasUID()) {
            String id = databaseUserNode.push().getKey();
            user.setUid(id);
        }
        user.toFireBase(databaseUserNode, storagePicNode);
    }

    public boolean hasUID(){
        return uid != "";
    }

    public void setUid(String id){
        uid = id;
    }

    public String getUid(){
        return uid;
    }

    private void toFireBase(DatabaseReference databaseUserNode, StorageReference storageImageNode){
        if(!hasUID()){
            Log.e(TAG+=" toFireBase", "try to add to firebase when no uid is assigned.");
            System.exit(1);
        }

        if(picture != null) {
            pictureId = uid;
            Bitmap bitmap = ((BitmapDrawable)picture).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = storageImageNode.child(pictureId).putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG += " toFireBase function", "upload picture bytes failure.");
                    System.exit(1);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    return;
                }
            });
        }
        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("bio", bio);
        result.put("email", email);
        result.put("gender", gender);
        if(pictureId != null){
            result.put("pictureId", pictureId);
        }
        databaseUserNode.child(uid).setValue(result);
    }



}

