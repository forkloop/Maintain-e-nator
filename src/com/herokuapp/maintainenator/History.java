package com.herokuapp.maintainenator;

import java.text.SimpleDateFormat;
import java.util.Date;

public class History {
    private String date;
    private String description;
    private String location;
    
    public History() {
        
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
    
    @Override
    public String toString() {
        return getDate() + "\n" + getLocation() + "\n" + getDescription();
    }

}
