package com.xuecheng.ucenter.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Author daydream
 * Description
 * Date 2024/9/14 11:05
 */
@Slf4j
@Component
public class CheckCodeClientFactory implements FallbackFactory<CheckCodeClient> {
    @Override
    public CheckCodeClient create(Throwable throwable) {
        return new CheckCodeClient() {
            @Override
            public Boolean verify(String key, String code) {
                log.debug("调用验证码服务熔断异常:{}", throwable.getMessage());
                return null;
            }
        };
    }
}
