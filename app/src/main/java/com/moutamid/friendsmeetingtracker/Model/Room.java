package com.moutamid.friendsmeetingtracker.Model;

import java.util.ArrayList;

public class Room {

    private String id;
    private String name;
    private String description;
    private ArrayList<String> users;
    private double meeting_lat;
    private double meeting_lng;

    public Room(){

    }

    public Room(String id, String name, String description,ArrayList<String> users,double meeting_lat, double meeting_lng) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.users = users;
        this.meeting_lat = meeting_lat;
        this.meeting_lng = meeting_lng;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getMeeting_lat() {
        return meeting_lat;
    }

    public void setMeeting_lat(double meeting_lat) {
        this.meeting_lat = meeting_lat;
    }

    public double getMeeting_lng() {
        return meeting_lng;
    }

    public void setMeeting_lng(double meeting_lng) {
        this.meeting_lng = meeting_lng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }
}
