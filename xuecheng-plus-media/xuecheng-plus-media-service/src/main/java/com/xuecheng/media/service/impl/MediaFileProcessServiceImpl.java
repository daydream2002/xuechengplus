package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @Author daydream
 * @Description
 * @Date 2024/8/13
 */
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {
    private final MediaProcessMapper mediaProcessMapper;
    private final MediaFilesMapper mediaFilesMapper;
    private final MediaProcessHistoryMapper mediaProcessHistoryMapper;

    public MediaFileProcessServiceImpl(MediaProcessMapper mediaProcessMapper, MediaFilesMapper mediaFilesMapper, MediaProcessHistoryMapper mediaProcessHistoryMapper) {
        this.mediaProcessMapper = mediaProcessMapper;
        this.mediaFilesMapper = mediaFilesMapper;
        this.mediaProcessHistoryMapper = mediaProcessHistoryMapper;
    }

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListBySharedIndex(shardIndex, shardTotal, count);
    }

    @Override
    public boolean startTask(long id) {
        int res = mediaProcessMapper.startTask(id);
        return res > 0;
    }

    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //要更新的任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null)
            return;
        //任务执行失败
        //更新MediaProcess表的状态
        if (Objects.equals(status, "3")) {
            mediaProcess.setStatus(status);
            mediaProcess.setFileId(fileId);
            mediaProcess.setErrormsg(errorMsg);
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcessMapper.updateById(mediaProcess);
        }
        //任务执行成功
        if (Objects.equals(status, "2")) {
            MediaFiles mediaFiles = new MediaFiles();
            //更新MediaFile表的url
            mediaFiles.setUrl(url);
            mediaFiles.setId(fileId);
            mediaFilesMapper.updateById(mediaFiles);
            //更新MediaProcess表的状态
            mediaProcess.setStatus(status);
            mediaProcess.setFinishDate(LocalDateTime.now());
            mediaProcess.setUrl(url);
            mediaProcessMapper.updateById(mediaProcess);
            //更新MediaProcess表的插入MediaProcessHistory表
            MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
            BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
            mediaProcessHistoryMapper.insert(mediaProcessHistory);
            //从MediaProcess表删除当前任务
            mediaProcessMapper.deleteById(taskId);
        }
    }
}
