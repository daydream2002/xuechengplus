package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.TransactionManagementConfigurationSelector;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author daydream
 * @Description
 * @Date 2024/7/18
 */
@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    TeachplanMapper teachplanMapper;
    ;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    @Transactional
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        Long id = saveTeachplanDto.getId();
        //课程id不存在，为新增操作
        if (id == null) {
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            Long parentid = teachplan.getParentid();
            Long courseId = teachplan.getCourseId();
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid, parentid)
                    .eq(Teachplan::getCourseId, courseId);
            List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);
            //获取Orderby字段的最大值
            List<Integer> collect = teachplans.stream().map(Teachplan::getOrderby).collect(Collectors.toList());
            int max = 0;
            for (Integer i : collect) {
                if (i > max)
                    max = i;
            }
            teachplan.setOrderby(max + 1);
            teachplanMapper.insert(teachplan);
        }
        //课程id存在，为修改操作
        else {
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    @Override
    public void deleteTeachplan(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        //查询是否为父节点
        if (teachplan.getGrade() == 1) {
            //查询子节点个数
            Integer nodeCount = teachplanMapper.selectCount(new LambdaQueryWrapper<Teachplan>().eq(Teachplan::getParentid, id));
            if (nodeCount > 0) {
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
            } else {
                teachplanMapper.deleteById(id);
            }
        } else {
            teachplanMapper.deleteById(id);
            //查询是否有关联的视频资源
            TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, id));
            if (teachplanMedia != null)
                teachplanMediaMapper.deleteById(teachplanMedia.getId());
        }
    }

    @Override
    public void moveTeachplan(String move, Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        Long parentid = teachplan.getParentid();
        Long courseId = teachplan.getCourseId();
        LambdaQueryWrapper<Teachplan> eq = new LambdaQueryWrapper<Teachplan>().eq(Teachplan::getParentid, parentid).eq(Teachplan::getCourseId, courseId);
        List<Teachplan> teachplans = teachplanMapper.selectList(eq);
        teachplans.sort(Comparator.comparing(Teachplan::getOrderby));
        int index = -1;
        for (int i = 0; i < teachplans.size(); i++) {
            if (Objects.equals(teachplans.get(i).getId(), id)) {
                index = i;
                break;
            }
        }
        //向下移动
        if (move.equals("movedown")) {
            if (index == teachplans.size() - 1) {
                XueChengPlusException.cast("已经在最下方，无法再进行下移");
            } else {
                Integer orderby1 = teachplans.get(index).getOrderby();
                Integer orderby2 = teachplans.get(index + 1).getOrderby();
                teachplans.get(index).setOrderby(orderby2);
                teachplans.get(index + 1).setOrderby(orderby1);
                teachplanMapper.updateById(teachplans.get(index));
                teachplanMapper.updateById(teachplans.get(index + 1));
            }
        } else {
            if (index == 0) {
                XueChengPlusException.cast("已经在最上方，无法再进行上移");
            } else {
                Integer orderby1 = teachplans.get(index).getOrderby();
                Integer orderby2 = teachplans.get(index - 1).getOrderby();
                teachplans.get(index).setOrderby(orderby2);
                teachplans.get(index - 1).setOrderby(orderby1);
                teachplanMapper.updateById(teachplans.get(index));
                teachplanMapper.updateById(teachplans.get(index - 1));
            }
        }
    }
}
