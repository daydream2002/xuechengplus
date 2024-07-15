package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;

import java.util.List;

/**
 * @Author daydream
 * @Description
 * @Date 2024/7/15
 */
@Data
public class CourseCategoryTreeDto extends CourseCategory implements java.io.Serializable {
    List<CourseCategoryTreeDto> childrenTreeNodes;
}
