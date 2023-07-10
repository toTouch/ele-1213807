package com.xiliulou.electricity.mq.data;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息数据
 *
 * @author xiaohui.song
 **/
@Data
public class MqDataMessageModel implements Serializable {

    /**
     * 消息内容体
     */
    private String body;

    /**
     * 链路ID
     */
    private String traceId;

}
