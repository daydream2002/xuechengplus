package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @Author daydream
 * @Description
 * @Date 2024/7/23
 */
public interface CourseTeacherService {
    List<CourseTeacher> list(Long courseId);

    CourseTeacher saveCourseTeacher(Long companyId,CourseTeacher courseTeacher);


    void deleteCourseTeacher(Long companyId, Long courseId, Long teacherId);
}
