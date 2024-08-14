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

    /**
     * 开启一个任务
     *
     * @param id
     * @return
     */
    public boolean startTask(long id);

    /**
     * @param taskId   任务id
     * @param status   任务状态
     * @param fileId   文件id
     * @param url      url
     * @param errorMsg 错误信息
     * @return void
     */
    void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);
}
