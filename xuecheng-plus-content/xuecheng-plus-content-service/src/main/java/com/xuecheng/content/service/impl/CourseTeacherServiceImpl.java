package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @Author daydream
 * @Description
 * @Date 2024/7/23
 */
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {
    private final CourseTeacherMapper courseTeacherMapper;
    private final CourseBaseMapper courseBaseMapper;

    public CourseTeacherServiceImpl(CourseTeacherMapper courseTeacherMapper, CourseBaseMapper courseBaseMapper) {
        this.courseTeacherMapper = courseTeacherMapper;
        this.courseBaseMapper = courseBaseMapper;
    }

    @Override
    public List<CourseTeacher> list(Long courseId) {
        return courseTeacherMapper.selectList(new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId, courseId));
    }

    @Override
    public CourseTeacher saveCourseTeacher(Long companyId, CourseTeacher courseTeacher) {

        Long courseId = courseTeacher.getCourseId();
        Long companyId1 = courseBaseMapper.selectById(courseId).getCompanyId();
        //新增课程
        if (courseTeacher.getId() == null) {
            if (!companyId1.equals(companyId)) {
                XueChengPlusException.cast("只允许机构在自己的课程中添加教师信息");
            }
            courseTeacher.setCreateDate(LocalDateTime.now());
            int courseTeacherId = courseTeacherMapper.insert(courseTeacher);
            return courseTeacherMapper.selectById(courseTeacherId);
        }
        //修改课程
        else {
            if (!companyId1.equals(companyId)) {
                XueChengPlusException.cast("只允许机构在自己的课程中修改教师信息");
            }
            courseTeacherMapper.updateById(courseTeacher);
            return courseTeacherMapper.selectById(courseTeacher.getId());
        }

    }

    @Override
    public void deleteCourseTeacher(Long companyId, Long courseId, Long teacherId) {
        if (!courseBaseMapper.selectById(courseId).getCompanyId().equals(companyId)) {
            XueChengPlusException.cast("只允许机构在自己的课程中删除教师信息");
        }
        courseTeacherMapper.deleteById(teacherId);
    }


}
