package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * Author daydream
 * Description
 * Date 2024/9/13 17:17
 */
public interface AuthService {
    XcUserExt execute(AuthParamsDto authParamsDto);
}
