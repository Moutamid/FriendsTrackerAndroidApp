package com.moutamid.friendsmeetingtracker.Model;

import java.util.ArrayList;

public class Room {

    private String id;
    private String name;
    private ArrayList<String> idList;


    public Room(){

    }

    public Room(String id, String name, ArrayList<String> idList) {
        this.id = id;
        this.name = name;
        this.idList = idList;
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

    public ArrayList<String> getIdList() {
        return idList;
    }

    public void setIdList(ArrayList<String> idList) {
        this.idList = idList;
    }
}
