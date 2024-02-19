package com.xiliulou.electricity.vo;
import lombok.Data;

/**
 * 统计某个租户下的柜机的数量的Vo
 *
 * @author maxiaodong
 * @since 2024-01-04 11:00:14
 */
@Data
public class ElectricityCabinetCountVO {
    /**
    * 换电柜Id
    */
    private Integer tenantId;
    
    /**
    * 柜机数量
    */
    private Integer cabinetCount;
}
