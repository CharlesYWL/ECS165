package com.example.s4966.ecs165.models;

import com.example.s4966.ecs165.utils.FirebasePaths;
import com.example.s4966.ecs165.utils.FirebaseUtil;

import java.util.List;

public class Postmodel {
    private String text;
    private String date_created;
    private String image_path;
    private String post_id;
    private String user_id;
    private String tags;
    private List<LikeModel> likes;
    private List<CommentModel> comments;

    /**
     *
     * @return the corresponding path in Firebase storage
     */
    public String getStorageImagePath(){
        String path = FirebasePaths.FIREBASE_POSTIMAGE_STORAGE_PATH + "/users"
                + "/" + user_id
                + "/" + date_created;
        return path;
    }
    public Postmodel(String text, String date_created, String image_path, String post_id, String user_id, String tags, List<LikeModel> likes, List<CommentModel> comments) {
        this.text = text;
        this.date_created = date_created;
        this.image_path = image_path;
        this.post_id = post_id;
        this.user_id = user_id;
        this.tags = tags;
        this.likes = likes;
        this.comments = comments;
    }

    public Postmodel(){
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public List<LikeModel> getLikes() {
        return likes;
    }

    public void setLikes(List<LikeModel> likes) {
        this.likes = likes;
    }

    public List<CommentModel> getComments() {
        return comments;
    }

    public void setComments(List<CommentModel> comments) {
        this.comments = comments;
    }
}
