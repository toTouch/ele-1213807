package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户绑定列表(ElectricityConfig)实体类
 *
 * @author zhangyongbo
 * @since 2023-10-11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_app_config")
public class ElectricityAppConfig {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long uid;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 是否开启选仓换电 （0--开启 1--关闭）
     */
    private Integer isSelectionExchange;
    
    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;
}
