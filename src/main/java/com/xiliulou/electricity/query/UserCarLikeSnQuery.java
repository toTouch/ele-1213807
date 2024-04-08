package com.xiliulou.electricity.query;


import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * Description: This class is UserCarLikeSnQuery! 用户根据车辆的SN码模糊查询 P0需求 15.1 实名用户列表（16条优化项）iv.4 模糊搜索车辆SN码
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#Nffzd1GUWoZOWAxqnV9cXzk2nQh">15.1  实名用户列表（16条优化项）iv.4 </a>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/3/13
 **/
@Data
@Builder
public class UserCarLikeSnQuery {
    
    /**
     * <p>
     * Description: 车辆SN码
     * </p>
     */
    private String carSn;
    
    /**
     * <p>
     * Description: 租户Id
     * </p>
     */
    private Long tenantId;
    
    /**
     * <p>
     * Description: 加盟商Id
     * </p>
     */
    private Long franchiseeId;
    
    /**
     * <p>
     * Description: 门店Id
     * </p>
     */
    private Long storeId;
    
    /**
     * <p>
     * Description: 分页起始偏移
     * </p>
     */
    private Long offset;
    
    
    /**
     * <p>
     * Description: 分页大小
     * </p>
     */
    private Long size;
}
