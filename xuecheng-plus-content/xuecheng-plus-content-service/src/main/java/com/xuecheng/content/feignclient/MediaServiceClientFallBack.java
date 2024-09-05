package com.xuecheng.content.feignclient;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Author daydream
 * Description
 * Date 2024/9/5 17:15
 */
@Component
public class MediaServiceClientFallBack implements MediaServiceClient {
    @Override
    //发生熔断，进入降级方法
    public String upload(MultipartFile filedata, String objectName) {
        return "";
    }
}
