package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 优惠券规则表(t_ele_tenant_map_key)实体类
 *
 * @author makejava
 * @since 2022-08-23 09:28:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_tenant_map_key")
public class EleTenantMapKey {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    /**
     * 地图key
     */
    private String mapKey;

    /**
     * 地图secret
     */
    private String mapSecret;

    private Long createTime;

    private Long updateTime;

    private Integer tenantId;
}
