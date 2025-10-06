package com.Mini_Instagram_Demo.Mini_Instagram_Demo.UploadImages.Repositories;

import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UploadImages.Models.Photo;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PhotoRepo extends JpaRepository<Photo , Integer> {
    List<Photo> findAllByUserId(int userId);

    List<Photo> findAllByUserIdIn(List<Integer> userIds);

    List<Photo> findTopByUserIdInOrderByCreationDateDesc(List<Integer> userIds, Pageable limit);
}
