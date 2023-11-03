package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-10-09-13:41
 */
@Data
public class CloudBeanGeneralViewVO {
    
    /**
     * 可分配云豆数
     */
    private Double canAllocationCloudBean;
    
    /**
     * 已分配云豆数量
     */
    private Double allocationCloudBean;
    
    /**
     * 已分配套餐数量
     */
    private int allocationMembercard;
    
    /**
     * 已分配人数
     */
    private long allocationUser;
    
    /**
     * 可回收云豆数
     */
    private Double canRecycleCloudBean;
    
    /**
     * 可回收套餐数
     */
    private int canRecycleMembercard;
    
    /**
     * 可回收用户
     */
    private long canRecycleUser;
    
    /**
     * 已回收云豆数
     */
    private Double recycleCloudBean;
    
    /**
     * 已回收套餐
     */
    private int recycleMembercard;
    
    /**
     * 已回收用户数
     */
    private long recycleUser;
}
