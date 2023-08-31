package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Kenneth
 * @Date: 2023/7/3 10:23
 * @Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EleCabinetUsedRecord {

    private Long id;

    private String userName;

    private String phone;

    /**
     * 1-租 ， 2-还， 9-换 换电没有类型，为独立的表，所以用数字9来区分
     */
    private Integer type;

    private String orderId;

    private String returnBatterySn;

    private Integer returnCellNo;

    private String leaseBatterySn;

    private Integer leaseCellNo;

    private Long createTime;

}
