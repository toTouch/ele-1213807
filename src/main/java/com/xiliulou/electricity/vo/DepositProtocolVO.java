package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/8/17 15:16
 * @Description:
 */

@Data
public class DepositProtocolVO {

    private Integer id;
    /**
     * 内容
     */
    private String content;


    private Long createTime;

    private Long updateTime;

    private Integer tenantId;

}
