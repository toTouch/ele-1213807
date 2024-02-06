package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/2/6 10:28
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_place_fee_record")
public class MerchantPlaceFeeRecord {
    /**
     * id
     */
    private Long id;
    
    /**
     * 柜机id
     */
    private Long cabinetId;
    /**
     *修改前场地费用（元）
     */
    private BigDecimal oldPlaceFee;
    
    /**
     * 修改后场地费用（元）
     */
    private BigDecimal newPlaceFee;
    /**
     *修改人id
     */
    private Long modifyUserId;
    
    /**
     * 租户id
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
}
