package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ElectricityCabinetModelQuery {
    private Long size;
    private Long offset;
    private String name;
    /***********12.2 电柜厂家型号（3条优化点）查询 start **********/
    /**
     * <p>
     *    Description: ---0不支持 ---1支持
     * </p>
    */
    private Integer heating;
    /***********12.2 电柜厂家型号（3条优化点）查询 end **********/
    /**
     * 厂家名称
     */
    private String manufacturerName;
    
    //租户id
    private Integer tenantId;
}
