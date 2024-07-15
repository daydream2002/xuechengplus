package com.xuecheng.content.service.impl;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author daydream
 * @Description
 * @Date 2024/7/15
 */
@SpringBootTest
class CourseCategoryServiceImplTest {
    @Autowired
    CourseCategoryService courseCategoryService;
    @Test
    public void courseCategoryServiceImplTest(){
        List<CourseCategoryTreeDto> list = courseCategoryService.queryTreeNodes("1");
        System.out.println(list);
    }

}