package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * @Author daydream
 * @Description
 * @Date 2024/7/12
 */
public interface CourseBaseInfoService {
    /**
     * 课程分页查询
     *
     * @param pageParams      分页查询参数
     * @param courseParamsDto 查询条件
     * @return 查询结果
     */
    PageResult<CourseBase> queryCourseBase(Long companyId,PageParams pageParams, QueryCourseParamsDto courseParamsDto);

    /**
     * 添加课程信息
     *
     * @param companyId
     * @param addCourseDto
     * @return
     */
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    /**
     * 根据课程id查询课程信息
     *
     * @param courseId
     * @return
     */
    CourseBaseInfoDto getCourseBaseById(Long courseId);

    /**
     * 根据id修改课程信息
     *
     * @param companyId
     * @param editCourseDto
     * @return
     */
    CourseBaseInfoDto modifyCourseBase(Long companyId, EditCourseDto editCourseDto);

    void deleteCourseBase(Long courseId);
}
