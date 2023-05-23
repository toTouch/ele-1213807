package com.xiliulou.electricity.query;

import lombok.Data;

/**
 * @author zgw
 * @date 2023/3/13 11:06
 * @mood
 */
@Data
public class ElectricityCabinetOrderExcelQuery {
    
    /**
     * 邀请用户uid
     */
    private Long uid;
    
    /**
     * 参与用户phone
     */
    private String joinPhone;
    
    /**
     * 参与人姓名
     */
    private String joinName;
    
    /**
     * 参与开始时间
     */
    private Long startTime;
    
    /**
     * 参与过期时间
     */
    private Long expiredTime;
    
    /**
     * 参与状态 1--初始化，2--已参与，3--已过期，4--被替换
     */
    private Integer status;
}
