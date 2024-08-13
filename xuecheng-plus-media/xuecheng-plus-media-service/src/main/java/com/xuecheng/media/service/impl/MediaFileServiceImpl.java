package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MediaFileService currentProxy;

    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;

    @Value("${minio.bucket.videofiles}")
    private String bucket_video;
    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    /**
     * 根据扩展名获取mimeType
     *
     * @param extension
     * @return
     */
    private String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用MimeType 字节流
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * 上传文件到minio
     *
     * @param localFilePath
     * @param mimeType
     * @param bucket
     * @param objectName
     * @return
     */

    private boolean addMediaFileToMinio(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .filename(localFilePath)
                    .object(objectName)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错,bucket:{},objectName:{},错误信息:{}", bucket, objectName, e.getMessage());
        }
        return false;
    }

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/") + "/";
        return folder;
    }

    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 文件信息入库
     *
     * @param companyId
     * @param fileMd5
     * @param uploadFileParamsDto
     * @param bucket
     * @param objectName
     * @return
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus("002003");
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                log.debug("向数据库保存文件失败,bucket:{},objectName:{}", bucket, objectName);
                XueChengPlusException.cast("文件保存失败");
            }
            addWaitingTask(mediaFiles);
        }
        return mediaFiles;
    }

    /**
     * 添加待处理任务
     *
     * @param mediaFiles 媒资文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles) {
        String filename = mediaFiles.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        if (mimeType.equals("video/x-msvideo")) {
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setStatus("1");
            mediaProcess.setFailCount(0);
            mediaProcessMapper.insert(mediaProcess);
        }
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(mediaFiles.getBucket())
                    .object(mediaFiles.getFilePath())
                    .build();
            try (FilterInputStream inputStream = minioClient.getObject(getObjectArgs)) {
                if (inputStream != null) {
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkFileFolderPath + chunkIndex)
                .build();
        try (FilterInputStream inputStream = minioClient.getObject(getObjectArgs)) {
            if (inputStream != null) {
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            log.error("对应文件块不存在");
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;
        //分块文件没有特定的文件类型
        String mimeType = getMimeType(null);
        //上传文件到minio
        boolean b = addMediaFileToMinio(localChunkFilePath, mimeType, bucket_video, chunkFilePath);
        if (!b) {
            return RestResponse.validfail(false, "上传分块文件失败");
        }
        return RestResponse.success(true);
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //找到分块文件
        List<ComposeSource> sources = Stream
                .iterate(0, i -> ++i)
                .limit(chunkTotal).map(i -> ComposeSource.builder().bucket(bucket_video).object(chunkFileFolderPath + i).build())
                .collect(Collectors.toList());
        //指定合并后的文件信息
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String objectName = getFilePathByMd5(fileMd5, extension);
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(objectName)
                .sources(sources)
                .build();
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            log.error("合并文件出错");
            return RestResponse.validfail(false, "合并文件异常");
        }
        //校验合并后的文件与源文件是否一致
        File file = downloadFileFromMinIO(bucket_video, objectName);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String mergeFileMd5 = DigestUtils.md5Hex(fileInputStream);
            if (!fileMd5.equals(mergeFileMd5)) {
                log.error("文件校验失败，合并后的文件与原始文件md5值不一致");
                return RestResponse.validfail(false, "文件校验失败");
            }
            uploadFileParamsDto.setFileSize(file.length());
        } catch (Exception e) {
            return RestResponse.validfail(false, "文件校验失败");
        } finally {
            if (file != null)
                file.delete();
        }
        //合并后的文件没有问题，将文件信息入库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
        //清理分块文件
        clearChunkFiles(chunkFileFolderPath, chunkTotal);
        return RestResponse.success(true);
    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        //获得文件相关信息
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        String defaultFolderPath = getDefaultFolderPath();

        String fileMd5 = getFileMd5(new File(localFilePath));

        String objectName = defaultFolderPath + fileMd5 + extension;
        //上传文件到minio
        boolean result = addMediaFileToMinio(localFilePath, mimeType, bucket_mediafiles, objectName);
        if (!result) {
            XueChengPlusException.cast("文件上传失败");
        }
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectName);
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    //得到合并后文件的地址
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

    /**
     * 从minio下载文件
     *
     * @param bucket     桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    // 从minio下载文件
    public File downloadFileFromMinIO(String bucket, String objectName) {
        File minioFile = null;
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build());
             FileOutputStream outputStream = new FileOutputStream(minioFile = File.createTempFile("minio", ".merge"))) {
            IOUtils.copy(stream, outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal) {
        List<DeleteObject> objects = Stream
                .iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> new DeleteObject(chunkFileFolderPath + i))
                .collect(Collectors.toList());
        RemoveObjectsArgs removeObjectArgs = RemoveObjectsArgs.builder().bucket(bucket_video).objects(objects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectArgs);
        results.forEach(item -> {
            try {
                DeleteError deleteError = item.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
