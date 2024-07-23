package com.xuecheng.content.api;


import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author daydream
 * @Description
 * @Date 2024/7/23
 */
@Api(value = "教师信息相关接口", tags = "教师信息相关接口")
@RestController
public class CourseTeacherController {
    @Autowired
    CourseTeacherService courseTeacherService;

    @ApiOperation("教师查询接口")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> list(@PathVariable Long courseId) {
        return courseTeacherService.list(courseId);
    }

    @ApiOperation("教师添加接口")
    @PostMapping("/courseTeacher")
    public CourseTeacher saveCourseTeacher(@RequestBody CourseTeacher courseTeacher) {
        Long companyId = 1232141425L;
        return courseTeacherService.saveCourseTeacher(companyId, courseTeacher);
    }
}
