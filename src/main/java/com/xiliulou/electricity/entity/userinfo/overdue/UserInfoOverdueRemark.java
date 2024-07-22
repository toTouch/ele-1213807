package com.xiliulou.electricity.entity.userinfo.overdue;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Description: This class is UserInfoOverdueRemark!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/6/25
 **/
@Data
@TableName("t_user_info_overdue_remark")
public class UserInfoOverdueRemark implements Serializable {
    
    @TableField(exist = false)
    private static final long serialVersionUID = -8382164031167485039L;
    
    /**
     * <p>
     *    Description: 主键
     * </p>
    */
    private Long id;
    
    /**
     * <p>
     *    Description: 0 -- 电,1 --车/车电一体
     * </p>
     */
    private Integer type;
    
    /**
     * <p>
     *    Description: 用户的UID
     * </p>
    */
    private Long uid;
    
    /**
     * <p>
     *    Description: 备注
     * </p>
    */
    private String remark;
    
    /**
     * <p>
     *    Description: 删除标识
     * </p>
    */
    private Integer delFlag;
    
    /**
     * <p>
     *    Description: 租户ID
     * </p>
    */
    private Long tenantId;
    
    public static final Integer TYPE_ELECTRICITY = 0;
    public static final Integer TYPE_CAR_ELECTRICITY = 1;
}
