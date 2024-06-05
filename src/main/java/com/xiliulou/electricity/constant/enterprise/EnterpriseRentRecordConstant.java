package com.xiliulou.electricity.constant.enterprise;

/**
 * @author maxiaodong
 * @date 2024/4/10 15:59
 * @desc
 */
public class EnterpriseRentRecordConstant {
    /**
     * 租退电套餐都是企业套餐
     */
    public static Integer ORDER_TYPE_ENTERPRISE = 0;
    
    /**
     * 租电为换电套餐，退电为企业套餐
     */
    public static Integer ORDER_TYPE_ELE_ENTERPRISE = 1;
    
    /**
     * 租退电套餐都是换电套餐
     */
    public static Integer ORDER_TYPE_ELE = 2;
    
    public static Integer ORDER_TYPE_OTHER = 3;
}
