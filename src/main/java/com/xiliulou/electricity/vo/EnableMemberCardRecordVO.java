package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author BaoYu
 * @description:
 * @date 2023/10/25 14:02
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnableMemberCardRecordVO {
    
    private Long id;
    
    /**
     * 用户姓名
     */
    private String userName;
    
    /**
     * 用户手机号
     */
    private String phone;
    
    /**
     * 套餐ID
     */
    private Long memberCardId;
    
    /**
     * 套餐名称
     */
    private String memberCardName;
    
    /**
     * 套餐业务类型：0，换电套餐；1，车电一体套餐, 2. 企业渠道换电套餐
     * @see BatteryMemberCardBusinessTypeEnum
     */
    private Integer businessType;
    
    /**
     * 启用类型 0--系统启用 1--人为启用
     */
    private Integer enableType;
    
    /**
     * 停卡天数
     */
    private Integer disableDays;
    
    /**
     * 电池服务费状态(。0--初始化 1--未支付服务费，2--已支付服务费)
     */
    private Integer batteryServiceFeeStatus;
    
    /**
     * 停卡单号
     */
    private String disableMemberCardNo;
    
    /**
     * 用户Id
     */
    private Long uid;
    
    /**
     * 停卡时间
     */
    private Long disableTime;
    
    /**
     * 启用时间
     */
    private Long enableTime;
    
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
     * 加盟商Id
     */
    private Long franchiseeId;
    
    private Long storeId;
    
    /**
     * 服务费金额
     */
    private BigDecimal serviceFee;
    
}
