package com.xiliulou.electricity.vo;


import lombok.Data;

/**
 * <p>
 * Description: This class is UserCarLikeVo! 用户根据车辆的SN码模糊返回
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/3/13
 **/
@Data
public class UserCarLikeVO {
    
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
