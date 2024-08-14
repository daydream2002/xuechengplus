package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class VideoTask {
    @Autowired
    MediaFileProcessService mediaFileProcessService;
    @Autowired
    MediaFileService mediaFileService;
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;


    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //确定cpu核心数
        int processors = Runtime.getRuntime().availableProcessors();
        //查询待处理的任务
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
        //任务数
        int size = mediaProcessList.size();
        if (size <= 0)
            return;
        //创建一个线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        //创建一个计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(
                mediaProcess -> {
                    //把任务加入线程池
                    executorService.execute(() -> {
                        try {
                            //任务执行的逻辑
                            Long taskId = mediaProcess.getId();
                            //开启任务
                            boolean b = mediaFileProcessService.startTask(taskId);
                            if (!b) {
                                log.debug("开启任务失败,任务id:{}", taskId);
                                return;
                            }
                            //下载minio文件到本地
                            String bucket = mediaProcess.getBucket();
                            String fileId = mediaProcess.getFileId();
                            String objectName = mediaProcess.getFilePath();
                            File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                            if (file == null) {
                                log.debug("视频下载出错,任务id:{},bucket:{},objectName:{}", taskId, bucket, objectName);
                                mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
                                return;
                            }

                            //原视频路径
                            String videoPath = file.getAbsolutePath();
                            //转换后视频名称
                            File mp4File = null;
                            try {
                                mp4File = File.createTempFile("minio", ".mp4");
                            } catch (IOException e) {
                                log.debug("创建临时文件异常，{}", e.getMessage());
                                mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件异常");
                                return;
                            }
                            String mp4Name = fileId + ".mp4";
                            String mp4Path = mp4File.getAbsolutePath();
                            Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, videoPath, mp4Name, mp4Path);
                            //开始视频转换
                            String result = videoUtil.generateMp4();
                            if (!result.equals("success")) {
                                log.debug("视频转码失败");
                                mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "视频转码失败");
                                return;
                            }
                            boolean b1 = mediaFileService.addMediaFileToMinio(mp4File.getAbsolutePath(), "video/mp4", bucket, getFilePath(fileId, ".mp4"));
                            if (!b1) {
                                log.debug("上传文件mp4到minio失败");
                                mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传文件到minio失败");
                                return;
                            }
                            String url = "/" + bucket + "/" + getFilePath(fileId, ".mp4");
                            mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, "");
                        } finally {
                            //计数器减1
                            countDownLatch.countDown();
                        }
                    });
                }
        );
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    private String getFilePath(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }


}
