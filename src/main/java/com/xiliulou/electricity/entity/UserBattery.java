package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * (UserBattery)表实体类
 *
 * @author zzlong
 * @since 2022-12-06 13:39:24
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_battery")
public class UserBattery {
    
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户id
     */
    private Long uid;
    
    /**
     * 初始电池编号
     */
    private String initBatterySn;
    
    /**
     * 当前电池编号
     */
    private String nowBatterySn;
    
    /**
     * 电池类型
     */
    private String batteryType;
    
    private Integer delFlag;
    
    private Long createTime;
    
    private Long updateTime;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}
