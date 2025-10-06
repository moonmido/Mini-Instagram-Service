package com.Mini_Instagram_Demo.Mini_Instagram_Demo.UserFollow.Repositories;

import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UserFollow.Models.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFollowRepo extends JpaRepository<UserFollow , Integer> {
    List<UserFollow> findByUserId1(int userId1);

    void deleteByUserId2(int userId2);

    void deleteUserFollowByUserId1AndUserId2(int userId1, int userId2);

    List<UserFollow> findByUserId2(int userId2);

    List<UserFollow> findAllByUserId1(int userId1);
}
