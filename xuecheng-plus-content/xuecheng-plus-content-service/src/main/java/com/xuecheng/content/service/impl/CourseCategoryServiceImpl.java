package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author daydream
 * @Description
 * @Date 2024/7/15
 */
@Service
@Slf4j
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> list = courseCategoryMapper.selectTreeNodes(id);
        //将list转成map,排除根节点
        Map<String, CourseCategoryTreeDto> map = list.stream().filter(item -> !id.equals(item.getId())).collect(Collectors.toMap(key -> key.getId(), value -> value));
        ArrayList<CourseCategoryTreeDto> courseList = new ArrayList<>();
        list.stream().filter(item -> !id.equals(item.getId())).forEach(
                item -> {
                    if (Objects.equals(item.getParentid(), id)) {
                        courseList.add(item);
                    }
                    CourseCategoryTreeDto itemParent = map.get(item.getParentid());
                    if (itemParent!=null){
                        if (itemParent.getChildrenTreeNodes()==null)
                            itemParent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                        itemParent.getChildrenTreeNodes().add(item);
                    }

                }
        );
        return courseList;
    }
}
