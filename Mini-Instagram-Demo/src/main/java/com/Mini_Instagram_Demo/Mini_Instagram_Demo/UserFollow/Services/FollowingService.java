package com.Mini_Instagram_Demo.Mini_Instagram_Demo.UserFollow.Services;

import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UserFollow.Models.UserFollow;
import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UserFollow.Repositories.UserFollowRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FollowingService {

    private final UserFollowRepo userFollowRepo;

    public FollowingService(UserFollowRepo userFollowRepo) {
        this.userFollowRepo = userFollowRepo;
    }

    // 🔹 عدد الأشخاص الذين يتابعهم المستخدم
    public int getNumberOfFollowing(int userId) {
        if (userId < 0) throw new IllegalArgumentException("Invalid userId");
        List<UserFollow> following = userFollowRepo.findByUserId1(userId);
        return following.size();
    }

    // 🔹 عدد الأشخاص الذين يتابعون المستخدم
    public int getNumberOfFollowers(int userId) {
        if (userId < 0) throw new IllegalArgumentException("Invalid userId");
        List<UserFollow> followers = userFollowRepo.findByUserId2(userId);
        return followers.size();
    }

    public void followUser(int followerId , int followingId){
        if(followerId<0 ||followingId<0) throw new IllegalArgumentException();
        UserFollow userFollow = new UserFollow();
        userFollow.setUserId1(followerId);
        userFollow.setUserId2(followingId);
        userFollowRepo.save(userFollow);
    }


    public void unfollowUser(int unfollowingId , int userId){
        if(unfollowingId<0) throw new IllegalArgumentException();
        userFollowRepo.deleteUserFollowByUserId1AndUserId2(userId,unfollowingId);
    }



}
