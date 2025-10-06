package com.Mini_Instagram_Demo.Mini_Instagram_Demo.DownloadImages.Services;

import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UploadImages.Models.Photo;
import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UploadImages.Repositories.PhotoRepo;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Optional;

@Service
public class DownloadingService {

    private final PhotoRepo photoRepo;


    public DownloadingService(PhotoRepo photoRepo) {
        this.photoRepo = photoRepo;
    }

    public Resource downloadImage(int photoId) throws MalformedURLException {
        if(photoId<0) throw new IllegalArgumentException();

        Optional<Photo> byId = photoRepo.findById(photoId);
        if(byId.isEmpty()) throw new RuntimeException("no Data found");
        Photo photo = byId.get();
        File file = new File(photo.getPhotoPath());
        return new UrlResource(file.toURI());
    }

}
