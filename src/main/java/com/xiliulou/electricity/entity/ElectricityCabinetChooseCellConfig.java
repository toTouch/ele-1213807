package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_choose_cell_config")
public class ElectricityCabinetChooseCellConfig {
    
    /**
     * id
     */
    private Long id;
    
    /**
     * 柜机型号
     */
    private Integer num;
    
    /**
     * 列，暂不使用扩展预留
     */
    private Integer column;
    
    /**
     * 中间排仓号
     */
    private String middleCell;
    
    /**
     * 最下面排仓号
     */
    private String belowCell;
    
    /**
     * 最上面排仓号
     */
    private String topCell;
    
    
    private Integer tenantId;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间（兼用在线时间）
     */
    private Long updateTime;
    
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;
    
}
