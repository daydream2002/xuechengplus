package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @Author daydream
 * @Description
 * @Date 2024/7/15
 */
@SpringBootTest
public class CourseCategoryMapperTests {
    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    @Test
    public void testCourseCategoryMapper(){
        List<CourseCategoryTreeDto> list = courseCategoryMapper.selectTreeNodes("1");
        System.out.println(list);
    }
}
