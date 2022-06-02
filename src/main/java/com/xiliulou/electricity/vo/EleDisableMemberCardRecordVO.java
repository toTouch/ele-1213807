package com.xiliulou.electricity.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 换电柜电池表(DisableMemberCardRecord)实体类
 *
 * @author makejava
 * @since 2022-06-02 14:44:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EleDisableMemberCardRecordVO {


    private Long userInfoId;

    /**
     * 月卡名称
     */
    private String memberCardName;

    /**
     * 停卡单号
     */
    private String disableMemberCardNo;

    /**
     * 停卡状态
     */
    private Integer status;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 错误原因
     */
    private String errMsg;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 租户id
     */
    private Integer tenantId;

    /**
     * 月卡过期时间
     */
    private Long memberCardExpireTime;

    /**
     * 月卡剩余天数
     */
    private Long cardDays;


}
