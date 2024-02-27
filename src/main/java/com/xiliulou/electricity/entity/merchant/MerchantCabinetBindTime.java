package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/2/26 15:37
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_cabinet_bind_time")
public class MerchantCabinetBindTime {
    private Long id;
    /**
     * 商户id
     */
    private Long merchantId;
    
    /**
     * 柜机Id
     */
    private Long cabinetId;
    
    /**
     * 场地id
     */
    private Long placeId;
    
    /**
     * 绑定时间
     */
    private Long bindTime;
    
    private Integer tenantId;
    
    private Integer delFlag;
    
    private Long createTime;
    
    private Long updateTime;
}
