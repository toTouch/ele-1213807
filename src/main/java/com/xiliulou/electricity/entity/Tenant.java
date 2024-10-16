package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 租户表(Tenant)实体类
 *
 * @author Eclair
 * @since 2021-06-16 14:31:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_tenant")
public class Tenant {
    /**
     * 租户id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 租户名称
     */
    private String name;
    /**
     * 租户编号
     */
    private String code;
    /**
     * 0正常 1-冻结
     */
    private Integer status;
    /**
     * 删除标记
     */
    private Integer delFlag;


    private Long createTime;

    private Long updateTime;

    //过期时间
    private Long expireTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    public static final Integer STA_NO_OUT = 0;
    public static final Integer STA_OUT = 1;

    public static final Integer ZHONGYING_TENANT_ID = 15;
    
    //超级管理员租户id
    public static final Integer SUPER_ADMIN_TENANT_ID = 1;

}
