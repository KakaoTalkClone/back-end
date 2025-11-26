package com.ocean.piuda.image.repository;

import com.ocean.piuda.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {


    @Query("""
    select i.url
    from UserImage ui
    join Image i on i.id = ui.image.id
    where ui.user.id = :userId
      and ui.type = com.ocean.piuda.image.enums.UserImageType.BACKGROUND
    """)
    List<String> findBackgroundImages(@Param("userId") Long userId);
}
