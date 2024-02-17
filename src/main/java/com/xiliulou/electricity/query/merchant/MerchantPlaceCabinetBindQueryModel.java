package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * @author maxiaodong
 * @date 2024/2/11 10:20
 * @desc
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantPlaceCabinetBindQueryModel {
    
    /**
     * 场地id
     */
    private List<Long> placeIdList;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 商户id
     */
    private Set<Long> merchantIdList;
    
    /**
     * 柜机id
     */
    private Integer cabinetId;
    
    /**
     * 判断绑定时间是否有重叠
     */
    private Long overlapTime;
    public final static Integer QUERY_CABINET_FLAG = 1;
    
    private Long size;
    
    private Long offset;
    
    private String sn;
    
    private Integer tenantId;
    
    private Long placeId;
}
