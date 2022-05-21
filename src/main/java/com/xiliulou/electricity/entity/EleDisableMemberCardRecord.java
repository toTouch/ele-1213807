package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 停卡记录表(TEleDisableMemberCardRecord)实体类
 *
 * @author makejava
 * @since 2022-05-21 10:17:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_disable_member_card_record")
public class EleDisableMemberCardRecord {
    /**
     * 停卡Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long uid;

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

    public static final Integer MEMBER_CARD_NOT_DISABLE = 0;
    public static final Integer MEMBER_CARD_DISABLE = 1;
    public static final Integer MEMBER_CARD_DISABLE_REVIEW = 2;

}
