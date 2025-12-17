package com.tngocnhat.shortformcontents;

import java.io.File;

public class Video {
    private String path;
    private String title;
    private int likeCount;
    private int dislikeCount;
    private boolean isLiked;
    private boolean isDisliked;

    public Video(String path, String title) {
        this.path = path;
        this.title = title;
        // Mock initial data
        this.likeCount = 200;
        this.dislikeCount = 200;
        this.isLiked = false;
        this.isDisliked = false;
    }

    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(int dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isDisliked() {
        return isDisliked;
    }

    public void setDisliked(boolean disliked) {
        isDisliked = disliked;
    }
}
