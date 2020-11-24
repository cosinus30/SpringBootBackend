package com.innova.service;

import java.util.List;

import com.innova.model.Tag;
import com.innova.repository.TagRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl implements TagService{
    
    @Autowired
    private TagRepository tagRepository;
    
    @Override
    public List<Tag> getAllTags() {
        List<Tag> tags = tagRepository.findAllByOrderByTagNameDesc();
        return tags;
    }
}
