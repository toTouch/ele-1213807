package com.xiliulou.electricity.constant.installment;

import com.xiliulou.electricity.annotation.ProcessParameter;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/27 15:24
 */
public interface InstallmentConstants {
    
    Integer PACKAGE_TYPE_BATTERY = 0;
    
    Integer PACKAGE_TYPE_CAR = 1;
    
    Integer PACKAGE_TYPE_CAR_BATTERY = 2;
    
    /**
     * 用于设置接口校验注解type，开启登录校验、数据权限设置
     *
     * @see ProcessParameter
     */
    int PROCESS_PARAMETER_DATA_PERMISSION = 1;
    
    /**
     * 用于设置接口校验注解type，开启登录校验、数据权限设置、分页参数校验
     *
     * @see ProcessParameter
     */
    int PROCESS_PARAMETER_LOGIN_AND_DATA_AND_PAGE = 3;
}
