package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-07-16:18
 */
@Data
public class InvitationActivityRecordInfoVO {

    private Long id;
    /**
     * 分享人数
     */
    private Integer shareCount;
    /**
     * 邀请成功人数
     */
    private Integer invitationCount;
    /**
     * 分享状态 1--初始化，2--已分享，3--分享失败
     */
    private Integer status;

    private BigDecimal money;



}
