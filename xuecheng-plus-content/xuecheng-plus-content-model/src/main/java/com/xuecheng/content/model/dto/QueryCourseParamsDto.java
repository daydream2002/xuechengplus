package com.xuecheng.content.model.dto;

import lombok.Data;

/**
 * @Author daydream
 * @Description 课程查询条件模型
 * @Date 2024/7/10
 */
@Data
public class QueryCourseParamsDto {
    //审核状态
    private String auditStatus;
    //课程名称
    private String courseName;
    //发布状态
    private String publishStatus;
}
