package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

/**
 * Author daydream
 * Description
 * Date 2024/9/24 10:29
 */
public interface WxAuthService {
    public XcUser wxAuth(String code);
}
