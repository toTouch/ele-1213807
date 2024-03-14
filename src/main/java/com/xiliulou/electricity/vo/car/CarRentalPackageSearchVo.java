package com.xiliulou.electricity.vo.car;


import lombok.Data;

/**
 * <p>
 *    Description: This class is CarRentalPackageSearchVo!
 *    14.4 套餐购买记录（2条优化项）
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/3/14
**/
@Data
public class CarRentalPackageSearchVo {
    /**
     * <p>
     *    Description: 套餐Id
     * </p>
    */
    private Long value;
    
    /**
     * <p>
     *    Description: 套餐名称
     * </p>
    */
    private String label;
}
