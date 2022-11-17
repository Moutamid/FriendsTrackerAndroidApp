package com.moutamid.friendsmeetingtracker.Model;

public class User {

    private String id;
    private String fullname;
    private String email;
    private String password;
    private String imageUrl;
    private double lat;
    private double lng;

    public User(){

    }


    public User(String id, String fullname, String email, String password, String imageUrl, double lat, double lng) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.password = password;
        this.imageUrl = imageUrl;
        this.lat = lat;
        this.lng = lng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
