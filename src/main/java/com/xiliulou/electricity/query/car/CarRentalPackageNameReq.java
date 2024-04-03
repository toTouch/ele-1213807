package com.xiliulou.electricity.query.car;


import lombok.Builder;
import lombok.Data;

/**
 * <p>
 *    Description: This class is CarRentalPackageNameReq!
 *    14.4 套餐购买记录（2条优化项）
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/3/14
**/
@Data
@Builder
public class CarRentalPackageNameReq {
    /**
     * <p>
     *    Description: 分页起始偏移
     * </p>
    */
    private Long offset;
    /**
     * <p>
     *    Description: 分页大小
     * </p>
     */
    private Long size;
    /**
     * <p>
     *    Description: 模糊搜索套餐名称
     * </p>
     */
    private String packageName;
    /**
     * <p>
     *    Description: 租户Id
     * </p>
     */
    private Long tenantId;
}
