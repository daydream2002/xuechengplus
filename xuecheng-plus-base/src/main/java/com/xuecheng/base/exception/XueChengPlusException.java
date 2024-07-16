package com.xuecheng.base.exception;

import lombok.Data;

/**
 * @Author daydream
 * @Description
 * @Date 2024/7/16
 */
@Data
public class XueChengPlusException extends RuntimeException {
    private String errMessage;

    public XueChengPlusException() {
    }

    public XueChengPlusException(String message) {
        super(message);
        this.errMessage = message;
    }

    public static void cast(String errMessage) {
        throw new XueChengPlusException(errMessage);
    }

    public static void cast(CommonError commonError) {
        throw new XueChengPlusException(commonError.getErrMessage());
    }
}
