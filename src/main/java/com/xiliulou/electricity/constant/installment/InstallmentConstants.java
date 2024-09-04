package com.xiliulou.electricity.constant.installment;

import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;

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
    
    /**
     * 分期记录状态-初始化
     */
    Integer INSTALLMENT_RECORD_STATUS_INIT = 0;
    
    /**
     * 分期记录状态-待签约
     */
    Integer INSTALLMENT_RECORD_STATUS_UN_SIGN = 1;
    
    /**
     * 分期记录状态-签约成功
     */
    Integer INSTALLMENT_RECORD_STATUS_SIGN = 2;
    
    /**
     * 分期记录状态-解约中
     */
    Integer INSTALLMENT_RECORD_STATUS_TERMINATE = 3;
    
    /**
     * 分期记录状态-已完成
     */
    Integer INSTALLMENT_RECORD_STATUS_COMPLETED = 4;
    
    /**
     * 代扣计划状态-未支付
     */
    Integer DEDUCTION_PLAN_STATUS_INIT = 0;
    
    /**
     * 代扣计划状态-已支付
     */
    Integer DEDUCTION_PLAN_STATUS_PAID = 1;
    
    /**
     * 代扣计划状态-代扣失败
     */
    Integer DEDUCTION_PLAN_STATUS_FAIL = 2;
    
    /**
     * 代扣计划状态-取消支付
     */
    Integer DEDUCTION_PLAN_STATUS_CANCEL = 3;
    
    /**
     * 蜂云成功响应码
     */
    String FY_SUCCESS_CODE = "WZF00000";
    
    /**
     * 蜂云回调业务参数名-bizContent
     */
    String OUTER_PARAM_BIZ_CONTENT = "bizContent";
    
    /**
     * 回调类型，签约成功
     */
    Integer NOTIFY_STATUS_SIGN = 1;
    
    /**
     * 回调类型，解约成功
     */
    Integer NOTIFY_STATUS_UN_SIGN = 2;
    
    /**
     * 客户端来源(h5)，h5模式返回url，可以直接访问
     */
    String CHANNEL_FROM_H5 = "h5";
}
