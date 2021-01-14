package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@Data
@Builder
public class RentCarOrderQuery {
    private Long size;
    private Long offset;
    /**
     * 用户名字
     */
    private String name;
    private String phone;
    /**
     * 门店Id
     */
    private Integer carStoreId;
    private Long beginTime;
    private Long endTime;
    private Integer status;
}
