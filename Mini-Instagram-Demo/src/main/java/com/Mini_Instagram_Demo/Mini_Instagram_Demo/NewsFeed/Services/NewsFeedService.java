package com.Mini_Instagram_Demo.Mini_Instagram_Demo.NewsFeed.Services;

import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UploadImages.Models.Photo;
import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UploadImages.Repositories.PhotoRepo;
import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UserFollow.Models.UserFollow;
import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UserFollow.Repositories.UserFollowRepo;
import jakarta.annotation.Resources;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsFeedService {

    private final UserFollowRepo userFollowRepo;
    private final PhotoRepo photoRepo;

    public NewsFeedService(UserFollowRepo userFollowRepo, PhotoRepo photoRepo) {
        this.userFollowRepo = userFollowRepo;
        this.photoRepo = photoRepo;
    }

    public List<Resource> getNewsFeedFollowedUsers(int followerId) throws MalformedURLException {
        if (followerId < 0)
            throw new IllegalArgumentException("Invalid followerId");

        // 1️⃣ جلب الأشخاص الذين يتابعهم المستخدم
        List<UserFollow> followingList = userFollowRepo.findAllByUserId1(followerId);
        if (followingList.isEmpty())
            throw new NullPointerException("No followed users found");

        // 2️⃣ استخراج الـ IDs للأشخاص الذين يتم متابعتهم
        List<Integer> followedUserIds = followingList.stream()
                .map(UserFollow::getUserId2)
                .collect(Collectors.toList());

        // 3️⃣ جلب آخر 100 صورة لهؤلاء المستخدمين
        Pageable top100 = PageRequest.of(0, 100);
        List<Photo> photos = photoRepo.findTopByUserIdInOrderByCreationDateDesc(followedUserIds, top100);

        // 4️⃣ تحويل كل صورة إلى Resource
        List<Resource> resources = photos.stream()
                .map(photo -> {
                    try {
                        return new UrlResource(photo.getPhotoPath());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("Invalid photo path: " + photo.getPhotoPath(), e);
                    }
                })
                .collect(Collectors.toList());

        return resources;
    }



}
