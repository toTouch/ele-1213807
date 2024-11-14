package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Description: This class is CouponDayRecordEntity!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/14
 **/
@Data
@TableName("t_coupon_day_record")
public class CouponDayRecordEntity implements Serializable {
    
    public static final Integer DEL_NORMAL = 0;
    
    private static final long serialVersionUID = -2675206847820983738L;
    
    /**
     * 主键
     **/
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 套餐id
     **/
    private Long packageId;
    
    /**
     * 优惠券id
     **/
    private Long couponId;
    
    /**
     * uid
     **/
    private Long uid;
    
    /**
     * 租户id
     **/
    private Long tenantId;
    
    /**
     * 增加天数
     **/
    private Integer days;
    
    /**
     * 0--车,1--电,2--车电一体
     **/
    private Integer useScope;
    
    /**
     * 创建时间
     **/
    private Long createTime;
    
    /**
     * 更新时间
     **/
    private Long updateTime;
    
    /**
     * 逻辑删除
     **/
    private Integer delFlag;
    
    
}
