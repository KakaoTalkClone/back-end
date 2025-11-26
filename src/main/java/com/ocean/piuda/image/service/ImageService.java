package com.ocean.piuda.image.service;


import com.ocean.piuda.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;

    public List<String> findBackgroundImages(Long userId){
        return imageRepository.findBackgroundImages(userId);
    }

}
