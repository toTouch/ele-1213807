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
     * 分期记录状态-已解约
     */
    Integer INSTALLMENT_RECORD_STATUS_CANCELLED = 5;
    
    /**
     * 分期记录状态-已取消
     */
    Integer INSTALLMENT_RECORD_STATUS_CANCEL_PAY = 6;
    
    /**
     * 分期记录状态-未支付服务费
     */
    Integer INSTALLMENT_RECORD_STATUS_UNPAID = 7;
    
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
     * 代扣计划状态-代扣中
     */
    Integer DEDUCTION_PLAN_STATUS_DEDUCTING = 4;
    
    /**
     * 代扣记录状态-代扣中
     */
    Integer DEDUCTION_RECORD_STATUS_INIT = 0;
    
    /**
     * 代扣记录状态-代扣成功
     */
    Integer DEDUCTION_RECORD_STATUS_SUCCESS = 1;
    
    /**
     * 代扣记录状态-代扣失败
     */
    Integer DEDUCTION_RECORD_STATUS_FAIL = 2;
    
    /**
     * 解约记录-审核中
     */
    Integer TERMINATING_RECORD_STATUS_INIT = 0;
    
    /**
     * 解约记录-拒绝
     */
    Integer TERMINATING_RECORD_STATUS_REFUSE = 1;
    
    /**
     * 解约记录-通过
     */
    Integer TERMINATING_RECORD_STATUS_RELEASE = 2;
    
    /**
     * 解约记录来源-用户或后台解约
     */
    Integer TERMINATING_RECORD_SOURCE_CANCEL = 0;
    
    /**
     * 解约记录来源-代扣完成
     */
    Integer TERMINATING_RECORD_SOURCE_COMPLETED = 1;
    
    /**
     * 蜂云成功响应码
     */
    String FY_RESULT_CODE_SUCCESS = "WZF00000";
    
    /**
     * 蜂云失败响应码-渠道码错误
     */
    String FY_RESULT_CODE_CHANNEL_CODE = "2001";
    
    /**
     * 蜂云失败响应码-余额不足
     */
    String FY_RESULT_CODE_INSUFFICIENT_BALANCE = "3001";
    
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
    
    /**
     * 客户端来源(miniapp)
     */
    String CHANNEL_FROM_MINIAPP = "miniapp";
    
    /**
     * 签约记录查询结果-签约成功
     */
    Integer SIGN_QUERY_STATUS_SIGN = 2;
    
    /**
     * 签约记录查询结果-解约成功
     */
    Integer SIGN_QUERY_STATUS_CANCEL = 3;
    
    /**
     * 代扣记录查询结果-扣款成功
     */
    String AGREEMENT_PAY_QUERY_STATUS_SUCCESS = "TRADE_SUCCESS";
    
    /**
     * 分期套餐最大天数
     */
    Integer INSTALLMENT_MAX_VALID_DAYS = 749;
    
    /**
     * 用户端查询代扣计划每期组合数据，未全部代扣完成
     */
    Integer PLAN_ASSEMBLY_STATUS_NOT_COMPLETE = 0;
    
    /**
     * 用户端查询代扣计划每期组合数据，已全部代扣完成
     */
    Integer PLAN_ASSEMBLY_STATUS_COMPLETED = 1;
}
