package com.xiliulou.electricity.vo;


import lombok.Data;

/**
 * <p>
 * Description: This class is UserCarLikeVo! 用户根据车辆的SN码模糊返回
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
public class UserCarLikeVo {
    
    /**
     * <p>
     * Description: 车辆Id
     * </p>
     */
    private Long id;
    
    /**
     * <p>
     * Description: sn码和车辆名的组合
     * </p>
     */
    private String label;
    
    /**
     * <p>
     * Description: 单独的sn码
     * </p>
     */
    private String sn;
}
