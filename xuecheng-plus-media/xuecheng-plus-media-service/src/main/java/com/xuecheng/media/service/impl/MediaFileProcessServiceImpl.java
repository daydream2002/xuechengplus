package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @Author daydream
 * @Description
 * @Date 2024/8/13
 */
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {
    private final MediaProcessMapper mediaProcessMapper;

    public MediaFileProcessServiceImpl(MediaProcessMapper mediaProcessMapper) {
        this.mediaProcessMapper = mediaProcessMapper;
    }

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListBySharedIndex(shardIndex, shardTotal, count);
        return mediaProcesses;
    }
}
