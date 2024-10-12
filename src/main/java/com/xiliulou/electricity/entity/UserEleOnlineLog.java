package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备上下线(TEleOnLineLog)实体类
 *
 * @author makejava
 * @since 2022-08-16 09:28:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEleOnlineLog {

    public static final Integer DEL_NORMAL = 0;

    public static final Integer DEL_DEL = 1;

    private Long id;

    /**
     * 换电柜id
     */
    private Integer electricityId;

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

}
