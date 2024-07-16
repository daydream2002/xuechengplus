package com.xuecheng.base.exception;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author daydream
 * @Description
 * @Date 2024/7/16
 */
@Setter
@Getter
public class RestErrorResponse implements Serializable {
    private String errMessage;

    public RestErrorResponse(String errMessage) {
        this.errMessage = errMessage;
    }

}
