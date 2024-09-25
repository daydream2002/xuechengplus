package com.xuecheng.content;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author daydream
 * @Description
 * @Date 2024/7/12
 */
@SpringBootTest
public class CourseBaseInfoServiceTests {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Test
    public void testCourseBaseInfoService() {
//        QueryCourseParamsDto courseParamsDto = new QueryCourseParamsDto();
//        PageParams pageParams = new PageParams(6L, 1L);
//        courseParamsDto.setCourseName("java");
//        courseParamsDto.setAuditStatus("202004");
////        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBase(pageParams, courseParamsDto);
//        System.out.println(courseBasePageResult);
    }
}
