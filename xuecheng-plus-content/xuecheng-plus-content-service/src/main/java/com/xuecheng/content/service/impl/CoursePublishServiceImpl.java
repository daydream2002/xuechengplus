package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Author daydream
 * Description
 * Date 2024/8/22
 */
@Service
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;
    @Autowired
    TeachplanService teachplanService;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @Autowired
    MqMessageService mqMessageService;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseById(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfo);
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseBaseById(courseId);
        if (courseBaseInfoDto == null) {
            XueChengPlusException.cast("课程找不到");
        }
        if (!companyId.equals(courseBaseInfoDto.getCompanyId())) {
            XueChengPlusException.cast("只能修改本机构自己的数据");
        }
        String auditStatus = courseBaseInfoDto.getAuditStatus();
        //如果课程已提交则不允许提交
        if (auditStatus.equals("202003")) {
            XueChengPlusException.cast("课程已提交请等待审核");
        }
        //课程的图片,计划信息没有填写也不允许提交
        String pic = courseBaseInfoDto.getPic();
        if (StringUtils.isEmpty(pic)) {
            XueChengPlusException.cast("请上传课程图片");
        }
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree == null || teachplanTree.isEmpty()) {
            XueChengPlusException.cast("请编写课程计划");
        }
        //查询课程基本信息,营销信息,计划信息插入到课程预发布表中
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfoDto, coursePublishPre);
        coursePublishPre.setCompanyId(companyId);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        String teachplanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeJson);
        //状态设置为已提交
        coursePublishPre.setStatus("202003");
        coursePublishPre.setCreateDate(LocalDateTime.now());
        CoursePublishPre coursePublishPreObj = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreObj == null) {
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Override
    @Transactional
    public void publish(Long companyId, Long courseId) {
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            XueChengPlusException.cast("课程没有审核记录，无法发布");
        }
        String status = coursePublishPre.getStatus();
        if (!status.equals("202004")) {
            XueChengPlusException.cast("课程没有审核通过不允许发布");
        }
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        //先查询课程发布，如果有则更新，如果没有再添加
        CoursePublish coursePublishObj = coursePublishMapper.selectById(courseId);
        if (coursePublishObj == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }
        //向消息表写数据
        saveCoursePublishMessage(courseId);
        //删除预发布表
        coursePublishPreMapper.deleteById(courseId);
    }
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }
}
