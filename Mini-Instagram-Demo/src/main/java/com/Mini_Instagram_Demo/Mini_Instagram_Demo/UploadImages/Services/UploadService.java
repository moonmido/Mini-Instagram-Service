package com.Mini_Instagram_Demo.Mini_Instagram_Demo.UploadImages.Services;

import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UploadImages.Inputs.LanLonInput;
import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UploadImages.Models.Photo;
import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UploadImages.Repositories.PhotoRepo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@Service
public class UploadService {

    private final PhotoRepo photoRepo;

    private static final String PARENT_PATH="C:\\Users\\Hp\\Desktop\\TestUploadInsta";

    public UploadService(PhotoRepo photoRepo) {
        this.photoRepo = photoRepo;
    }


    public Photo uploadImage(int userId , MultipartFile img , LanLonInput lanLonInput) throws IOException {
        if(userId<0 ||img.isEmpty() || lanLonInput==null) throw new IllegalArgumentException();

        File directory = new File(PARENT_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = userId+"_"+img.getOriginalFilename();
        File file = new File(PARENT_PATH+File.separator+fileName);

        img.transferTo(file);
        Photo photo = new Photo();
        photo.setUserId(userId);
        photo.setPhotoLatitude(lanLonInput.photoLan());
        photo.setPhotoLongitude(lanLonInput.photoLon());
        photo.setUserLatitude(lanLonInput.userLan());
        photo.setUserLongitude(lanLonInput.userLon());
        photo.setCreationDate(new Date());
        photo.setPhotoPath(file.getAbsolutePath());

        return photoRepo.save(photo);

    }





}
