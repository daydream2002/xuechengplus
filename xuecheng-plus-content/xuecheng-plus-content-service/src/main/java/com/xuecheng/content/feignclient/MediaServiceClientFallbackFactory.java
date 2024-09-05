package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Author daydream
 * Description
 * Date 2024/9/5 17:17
 */
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return (filedata, objectName) -> null;
    }
}
