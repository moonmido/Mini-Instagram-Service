package com.Mini_Instagram_Demo.Mini_Instagram_Demo.ManageUsers.Services;

import com.Mini_Instagram_Demo.Mini_Instagram_Demo.ManageUsers.Repositories.UserRepo;
import com.Mini_Instagram_Demo.Mini_Instagram_Demo.ManageUsers.Models.User;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserService {

    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    // 🔹 إضافة مستخدم جديد
    public User addUser(User user) {
        user.setCreationDate(new Date()); // تحديد تاريخ الإنشاء تلقائياً
        user.setLastLogin(new Date());    // آخر تسجيل دخول أولي
        return userRepo.save(user);
    }

    // 🔹 تحديث مستخدم موجود
    public User updateUser(int userId, User updatedUser) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());
        user.setDateOfBirth(updatedUser.getDateOfBirth());
        user.setLastLogin(new Date()); // يمكن تحديث آخر تسجيل دخول عند أي تعديل
        return userRepo.save(user);
    }

    // 🔹 حذف مستخدم
    public void deleteUser(int userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepo.delete(user);
    }

    // 🔹 جلب مستخدم حسب الـ ID
    public User getUserById(int userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 🔹 جلب جميع المستخدمين
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}

