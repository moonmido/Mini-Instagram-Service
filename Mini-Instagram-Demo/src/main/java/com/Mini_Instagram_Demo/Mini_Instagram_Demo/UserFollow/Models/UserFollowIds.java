package com.Mini_Instagram_Demo.Mini_Instagram_Demo.UserFollow.Models;

import java.io.Serializable;
import java.util.Objects;

public class UserFollowIds implements Serializable {

    private int userId1;
    private int userId2;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserFollowIds key)) return false;
        return Objects.equals(userId1, key.userId1) &&
                Objects.equals(userId2, key.userId2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId1, userId2);
    }


}
