package com.xiliulou.electricity.entity.meituan;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 美团骑手商城配置信息实体类
 * @date 2024/8/28 10:26:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_mei_tuan_rider_mall_config")
public class MeiTuanRiderMallConfig {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * appId
     */
    private String appId;
    
    /**
     * appKey
     */
    private String appKey;
    
    /**
     * secret
     */
    private String secret;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 删除标志，默认0表示正常，1表示已删除
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
}
