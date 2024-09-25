package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.IntBuffer;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author daydream
 * @Description
 * @Date 2024/7/12
 */
@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Transactional
    @Override
    public PageResult<CourseBase> queryCourseBase(Long companyId, PageParams pageParams, QueryCourseParamsDto courseParamsDto) {
        //创建查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseBase::getCompanyId, companyId);
        //根据名称模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()), CourseBase::getName, courseParamsDto.getCourseName());
        //根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, courseParamsDto.getAuditStatus());
        //根据课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getPublishStatus()), CourseBase::getStatus, courseParamsDto.getPublishStatus());
        //创建分页条件
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //根据条件进行分页查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        //获得查询结果
        List<CourseBase> records = pageResult.getRecords();
        //总记录数
        long total = pageResult.getTotal();
        return new PageResult<>(records, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Override
    @Transactional
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        //合法性校验
        /*if (StringUtils.isBlank(dto.getName())) {
            throw new XueChengPlusException("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new XueChengPlusException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new XueChengPlusException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new XueChengPlusException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new XueChengPlusException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new XueChengPlusException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new XueChengPlusException("收费规则为空");
        }*/
        //向course_base表写入数据
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(dto, courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        int insert = courseBaseMapper.insert(courseBase);
        if (insert < 0) {
            throw new XueChengPlusException("插入数据库失败");
        }
        //向course_market写入数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);
        courseMarket.setId(courseBase.getId());
        saveCourseMarket(courseMarket);
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseBase.getId());

        //查询课程信息
        return courseBaseInfo;
    }

    @Override
    public CourseBaseInfoDto getCourseBaseById(Long courseId) {
        return getCourseBaseInfo(courseId);
    }

    @Override
    @Transactional
    public CourseBaseInfoDto modifyCourseBase(Long companyId, EditCourseDto editCourseDto) {
        Long courseId = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            XueChengPlusException.cast("课程不存在");
        }
        if (!companyId.equals(courseBase.getCompanyId())) {
            XueChengPlusException.cast("只能修改本机构自己的数据");
        }


        BeanUtils.copyProperties(editCourseDto, courseBase);
        courseBase.setCreateDate(LocalDateTime.now());
        courseBaseMapper.updateById(courseBase);


        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto, courseMarket);
        saveCourseMarket(courseMarket);

        return getCourseBaseInfo(courseId);
    }

    @Override
    @Transactional
    public void deleteCourseBase(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!courseBase.getStatus().equals("203001")) {
            XueChengPlusException.cast("只能删除未发布的课程信息");
        }
        //删除课程基本信息
        courseBaseMapper.deleteById(courseId);
        //删除课程营销信息
        courseMarketMapper.deleteById(courseId);
        //删除课程计划
        teachplanMapper.delete(new LambdaQueryWrapper<Teachplan>().eq(Teachplan::getCourseId, courseId));
        //删除课程计划关联信息
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getCourseId, courseId));
        //删除课程师资信息
        courseTeacherMapper.delete(new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId, courseId));
    }


    private CourseBaseInfoDto getCourseBaseInfo(Long id) {
        CourseBase courseBase = courseBaseMapper.selectById(id);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        if (courseBase == null)
            return null;
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        if (courseMarket != null)
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        courseBaseInfoDto.setMtName(courseCategoryMapper.selectById(courseBaseInfoDto.getMt()).getName());
        courseBaseInfoDto.setStName(courseCategoryMapper.selectById(courseBaseInfoDto.getSt()).getName());
        return courseBaseInfoDto;
    }

    private void saveCourseMarket(CourseMarket courseMarket) {
        String charge = courseMarket.getCharge();
        if (charge.isEmpty()) {
            throw new XueChengPlusException("收费规则为空");
        }
        if (charge.equals("201001")) {
            if (courseMarket.getPrice() == null || courseMarket.getPrice() <= 0) {
                throw new XueChengPlusException("课程价格不能为空且大于0");
            }
        }
        Long id = courseMarket.getId();
        CourseMarket courseMarket1 = courseMarketMapper.selectById(id);
        if (courseMarket1 == null) {
            courseMarketMapper.insert(courseMarket);
        } else {
            courseMarketMapper.updateById(courseMarket);
        }

    }
}
