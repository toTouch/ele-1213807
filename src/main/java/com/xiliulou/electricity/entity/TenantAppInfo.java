package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (TenantAppInfo)实体类
 *
 * @author Eclair
 * @since 2021-07-21 09:57:44
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_tenant_app_info")
public class TenantAppInfo {

    private Integer id;
    /**
     * 应用类型 MT-美团，CUPBOARD-餐柜api
     */
    private String type;
    /**
     * appId
     */
    private String appid;
    /**
     * appSecert
     */
    private String appsecert;
    /**
     * 所属租户id
     */
    private Integer tenantId;
    /**
     * 0--正常使用 1--停用
     */
    private Integer status;

    private Long createTime;

    public static final Integer TYPE_NORMAL = 0;
    public static final Integer TYPE_STOP = 1;

    public static final String MT_TYPE = "MT";

    public static final String CUPBOARD_TYPE = "CUPBOARD";


}
