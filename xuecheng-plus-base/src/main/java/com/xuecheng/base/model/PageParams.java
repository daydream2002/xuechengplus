package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Mr.M
 * @version 1.0
 * @description 分页查询分页参数
 * @date 2023/2/11 15:33
 */
@Data
public class PageParams {

    //当前页码
    @ApiModelProperty("页码")
    private Long pageNo = 1L;
    //每页显示记录数
    @ApiModelProperty("每页记录数")
    private Long pageSize = 30L;

    public PageParams() {

    }

    public PageParams(Long pageSize, Long pageNo) {
        this.pageSize = pageSize;
        this.pageNo = pageNo;
    }
}
