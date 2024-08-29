package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

/**
 * Author daydream
 * Description
 * Date 2024/8/22
 */
public interface CoursePublishService {
    /**
     * 获取课程预览信息
     *
     * @param courseId
     * @return
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    void commitAudit(Long companyId, Long courseId);

    void publish(Long companyId, Long courseId);
}
