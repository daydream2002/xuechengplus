package com.xuecheng.media;

import com.alibaba.nacos.common.utils.IoUtils;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @Author daydream
 * @Description 测试minio SDK接口
 * @Date 2024/7/25
 */
public class MinioTest {
    MinioClient minioClient = MinioClient.builder()
            .endpoint("http://192.168.101.65:9000")
            .credentials("minioadmin", "minioadmin")
            .build();

    @Test
    public void test_upload() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".docx");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            String mimeType1 = extensionMatch.getMimeType();
        }

        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("testbucket")
                .filename("D:\\Users\\Desktop\\机器学习与大数据工程研究中心项目申请书(4).doc")
                .object("1.docx")
                .contentType(mimeType)
                .build();
        minioClient.uploadObject(uploadObjectArgs);
    }

    @Test
    public void test_delete() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket("testbucket")
                .object("1.docx")
                .build();
        minioClient.removeObject(removeObjectArgs);
    }

    @Test
    public void test_get() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket("testbucket")
                .object("1.docx")
                .build();
        FilterInputStream filterInputStream = minioClient.getObject(getObjectArgs);
        FileOutputStream fileOutputStream = new FileOutputStream("D:\\");
        IoUtils.copy(filterInputStream, fileOutputStream);
    }
}
