package com.xiliulou.electricity.vo.enterprise;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-14-10:30
 */
@Data
public class EnterpriseChannelUserExitVO {
    /**
     * 用户名称
     */
    private String username;
    
    /**
     * 用户电话
     */
    private String phone;
    
    /**
     * 用户状态: 0-套餐已冻结,1-套餐冻结申请中,2-未退还电池,3-滞纳金未缴纳
     */
    private Integer status;
    
    /**
     * 电池编号
     */
    private String batterySn;
    
    public final static Integer FREE = 0;
    public final static Integer FREE_APPLY = 1;
    public final static Integer BATTERY_EXIT = 2;
    public final static Integer SERVICE_FEE = 3;
}
