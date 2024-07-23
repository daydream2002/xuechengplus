package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author daydream
 * @Description
 * @Date 2024/7/18
 */
@Data
public class EditCourseDto extends AddCourseDto {

    @ApiModelProperty(value = "课程id", required = true)
    private Long id;
}
