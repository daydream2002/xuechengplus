package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;
import jdk.internal.dynalink.linker.LinkerServices;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author daydream
 * @Description
 * @Date 2024/8/13
 */
public interface MediaFileProcessService {
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);
}
