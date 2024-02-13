package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/2/2 11:33
 * @desc 商户场地映射表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_place_map")
public class MerchantPlaceMap {
    
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
    
    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;
}
