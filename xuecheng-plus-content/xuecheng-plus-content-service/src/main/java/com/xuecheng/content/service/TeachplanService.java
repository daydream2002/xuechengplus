package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * @Author daydream
 * @Description 课程计划管理相关接口
 * @Date 2024/7/18
 */
public interface TeachplanService {
    /**
     * 根据课程id查询课程计划
     *
     * @param courseId
     * @return
     */
    public List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
     * 新增或修改课程计划
     *
     * @param saveTeachplanDto
     */
    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     * 删除课程
     *
     * @param id
     */
    void deleteTeachplan(Long id);

    /**
     * 课程移动
     *
     * @param move
     * @param id
     */
    void moveTeachplan(String move, Long id);

    void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    void disAssociationMedia(Long teachPlanId, String mediaId);
}
