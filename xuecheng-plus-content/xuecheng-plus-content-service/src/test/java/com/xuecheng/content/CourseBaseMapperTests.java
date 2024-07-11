package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @Author daydream
 * @Description
 * @Date 2024/7/11
 */
@SpringBootTest
public class CourseBaseMapperTests {
    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Test
    public void testCourseBaseMapper() {

        QueryCourseParamsDto courseParamsDto = new QueryCourseParamsDto();
        courseParamsDto.setCourseName("java");
        //创建查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //根据名称模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()), CourseBase::getName, courseParamsDto.getCourseName());
        //根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, courseParamsDto.getAuditStatus());
        PageParams pageParams = new PageParams(6L, 1L);
        //创建分页条件
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //根据条件进行分页查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        //获得查询结果
        List<CourseBase> records = pageResult.getRecords();
        //总记录数
        long total = pageResult.getTotal();
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(records, total, pageParams.getPageNo(), pageParams.getPageSize());
        System.out.println(courseBasePageResult);
    }
}
