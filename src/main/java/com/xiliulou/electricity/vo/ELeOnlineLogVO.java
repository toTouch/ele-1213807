package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author: Miss.Li
 * @Date: 2022/08/16 10:03
 * @Description:
 */
@Data
public class ELeOnlineLogVO {

    private Long id;

    /**
     * 换电柜id
     */
    private Integer eleId;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * offline下线，online上线
     */
    private String status;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 上报时间
     */
    private String appearTime;

    /**
     * 创建时间
     */
    private Long createTime;

    private String name;
}
