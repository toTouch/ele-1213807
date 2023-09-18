package com.xiliulou.electricity.vo.enterprise;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-18-13:54
 */
@Data
public class UserCloudBeanDetailVO {

    /**
     * 云豆总数
     */
    private BigDecimal totalCloudBean;
    /**
     * 已分配云豆
     */
    private Double distributableCloudBean;
    /**
     * 已回收云豆
     */
    private Double recoveredCloudBean;
    /**
     * 可回收云豆
     */
    private Double recyclableCloudBean;
}
