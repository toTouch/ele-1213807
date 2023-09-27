package com.xiliulou.electricity.vo.activity;

import com.xiliulou.electricity.enums.PackageTypeEnum;
import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/8/12 17:01
 * @Description:
 */

@Data
public class ActivityPackageVO {

    private Long packageId;

    private String packageName;

    /**
     * 套餐类型
     *
     * @see PackageTypeEnum
     */
    private Integer packageType;

}
