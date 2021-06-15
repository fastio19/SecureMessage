package com.example.securemessage.Models;

public class Users {
    String profilePic,userName,email,password,userId,lastMessage,status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Users(){

    }
    public Users(String email,String password,String profilePic,String status,String userId,
                 String userName){
        this.email=email;
        this.password=password;
        this.profilePic=profilePic;
        this.status=status;
        this.userId=userId;
        this.userName=userName;
    }
    public Users(String profilePic, String userName, String email, String password, String userId,
                 String lastMessage, String status) {
        this.profilePic = profilePic;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.userId = userId;
        this.lastMessage = lastMessage;
        this.status = status;
    }

    // SignUp constructor
    public Users(String userName, String email,
                 String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    public String getUserId(String key) {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getUserId() {
        return userId;
    }
}
