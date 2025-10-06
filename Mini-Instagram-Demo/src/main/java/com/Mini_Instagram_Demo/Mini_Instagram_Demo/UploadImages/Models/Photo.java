package com.Mini_Instagram_Demo.Mini_Instagram_Demo.UploadImages.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Date;

@Entity
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int PhotoId;

    private int userId;
    private String photoPath;
    private int userLatitude;
    private int userLongitude;
    private int photoLatitude;
    private int photoLongitude;
    private Date CreationDate;

    public int getPhotoId() {
        return PhotoId;
    }

    public void setPhotoId(int photoId) {
        PhotoId = photoId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public double getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(int userLatitude) {
        this.userLatitude = userLatitude;
    }

    public int getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(int userLongitude) {
        this.userLongitude = userLongitude;
    }

    public int getPhotoLatitude() {
        return photoLatitude;
    }

    public void setPhotoLatitude(int photoLatitude) {
        this.photoLatitude = photoLatitude;
    }

    public int getPhotoLongitude() {
        return photoLongitude;
    }

    public void setPhotoLongitude(int photoLongitude) {
        this.photoLongitude = photoLongitude;
    }

    public Date getCreationDate() {
        return CreationDate;
    }

    public void setCreationDate(Date creationDate) {
        CreationDate = creationDate;
    }
}
