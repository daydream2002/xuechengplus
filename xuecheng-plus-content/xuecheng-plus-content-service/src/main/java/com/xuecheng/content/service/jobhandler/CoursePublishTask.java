package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Author daydream
 * Description
 * Date 2024/9/2
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {
    @Autowired
    CoursePublishService coursePublishService;

    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() {
        //分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //调用抽象类的方法执行任务
        process(shardIndex, shardTotal, "course_publish", 30, 60);
    }

    //执行发布任务的逻辑
    @Override
    public boolean execute(MqMessage mqMessage) {
        long courseId = Long.parseLong(mqMessage.getBusinessKey1());
        //课程静态化上传minio
        generateCourseHtml(mqMessage, courseId);
        //向elasticsearch写索引
        saveCourseIndex(mqMessage, courseId);
        //向redis写缓存
        return true;
    }

    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo > 0) {
            log.debug("课程静态化任务完成");
            return;
        }
        //开始课程静态化
//        int i = 1 / 0;

        //任务完成
        mqMessageService.completedStageTwo(taskId);
    }

    private void generateCourseHtml(MqMessage mqMessage, long courseId) {
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne > 0) {
            log.debug("课程静态化任务完成");
            return;
        }
        //开始课程静态化
        File file = coursePublishService.generateCourseHtml(courseId);
        if (file == null) {
            XueChengPlusException.cast("生成的静态页面为空");
        }
        coursePublishService.uploadCourseHtml(courseId, file);
        //任务完成
        mqMessageService.completedStageOne(taskId);
    }
}
