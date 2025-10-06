package com.Mini_Instagram_Demo.Mini_Instagram_Demo.ManageUsers.Repositories;

import com.Mini_Instagram_Demo.Mini_Instagram_Demo.ManageUsers.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User , Integer> {
}
