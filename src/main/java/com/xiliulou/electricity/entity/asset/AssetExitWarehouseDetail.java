package com.xiliulou.electricity.entity.asset;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 退库详情表
 * @date 2023/11/27 16:52:39
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_asset_exit_warehouse_detail")
public class AssetExitWarehouseDetail {
    /**
     * 主键Id
     */
    private Long id;
    
    /**
     * 退库单号
     */
    private String orderNo;
    
    /**
     * 退库类型(1-电柜, 2-电池, 3-车辆)
     */
    private Integer type;
    
    /**
     * SN码
     */
    private String sn;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 备注
     */
    private String remark;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
}
