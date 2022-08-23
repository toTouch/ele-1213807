package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 活动表(t_ele_tenant_map_key)实体类
 *
 * @author makejava
 * @since 2022-08-22 09:27:12
 */
@Data
public class EleTenantMapKeyAddAndUpdate {

    /**
     * 地图key
     */
    @NotEmpty(message = "地图key不能为空!")
    private String mapKey;


    /**
     * 地图key
     */
    @NotEmpty(message = "地图secret不能为空!")
    private String mapSecret;




}


