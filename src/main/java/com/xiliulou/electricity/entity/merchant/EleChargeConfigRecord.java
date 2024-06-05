package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 电价配置记录表
 * @date 2024/2/6 13:34:14
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_charge_config_record")
public class EleChargeConfigRecord {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 电价名称
     */
    private String name;
    
    /**
     * 所属加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 所属门店id
     */
    private Long storeId;
    
    /**
     * 所属换电柜Id
     */
    private Long eid;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 数据类型 0--加盟商全部（运营商） 1--门店全部（加盟商）2--柜机
     */
    private Integer type;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 规则
     */
    private String jsonRule;
    
    /**
     * 操作类型：1-新增 2-修改 3-删除
     */
    private Integer operationType;
    
    /**
     * 操作时间
     */
    private Long operationTime;
    
    /**
     * 操作人
     */
    private Long operator;
    
    /**
     * 电价配置ID
     */
    private Long configId;
    
}
