package com.ab.sync;

public class Members {
    String userName,userID,imageUrl;

    public Members(){

    }

    public Members(String userName, String userID, String imageUrl) {
        this.userName = userName;
        this.userID = userID;
        this.imageUrl = imageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
