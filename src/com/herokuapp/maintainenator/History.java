package com.herokuapp.maintainenator;

import java.text.SimpleDateFormat;
import java.util.Date;

public class History {
    private String date;
    private String description;
    private String location;
    private String photosPath;
    private String audioPath;

    public History () {
    }

    public History (String description, String location) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date();
        String d = format.format(date);
        setDate(d);
        setDescription(description);
        setLocation(location);
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPhotosPath(String path) {
        this.photosPath = path;
    }

    public String getPhotosPath() {
        return this.photosPath;
    }

    public void setAudioPath(String path) {
        if (path == null) {
            path = "";
        }
        this.audioPath = path;
    }

    public String getAudioPath() {
        return this.audioPath;
    }

    @Override
    public String toString() {
        return getDescription() + "@ " + getLocation();
    }

}