package com.Mini_Instagram_Demo.Mini_Instagram_Demo.UserFollow.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Entity
@IdClass(UserFollowIds.class)
public class UserFollow {

    @Id
    private int userId1;

    @Id
    private int userId2;

    public int getUserId1() {
        return userId1;
    }

    public void setUserId1(int userId1) {
        this.userId1 = userId1;
    }

    public int getUserId2() {
        return userId2;
    }

    public void setUserId2(int userId2) {
        this.userId2 = userId2;
    }
}
