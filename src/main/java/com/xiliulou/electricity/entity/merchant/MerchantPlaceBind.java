package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/2/2 11:37
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_place_bind")
public class MerchantPlaceBind {
    /**
     * id
     */
    private Long id;
    /**
     * 商户id
     */
    private Long merchantId;
    /**
     * 场地id
     */
    private Long placeId;
    /**
     * 绑定时间
     */
    private Long bindTime;
    /**
     * 解绑时间
     */
    private Long unBindTime;
    /**
     * 类型(1-解绑，0-绑定)
     */
    private Integer type;
    /**
     * 租户Id
     */
    private Integer tenantId;
    /**
     * 删除标记(0-未删除，1-已删除)
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 修改时间
     */
    private Long updateTime;
    
    /**
     * 商户结算标记(0-是，1-否)
     */
    private Integer merchantMonthSettlement;
    
    /**
     * 商户电费结算标记(0-是，1-否)
     */
    private Integer merchantMonthSettlementPower;
    
    /**
     * 是否大于出账月份的月末 0-是，1-否
     */
    private Integer isBigMonthEndFlag;
}
