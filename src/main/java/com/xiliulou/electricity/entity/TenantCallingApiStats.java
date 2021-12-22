package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Miss.Li
 * @Date: 2021/11/5 10:21
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_tenant_calling_api_stats")
public class TenantCallingApiStats {

    private Integer id;
    /**
    * 租户id
    */
    private Integer tenantId;
    /**
    * api调用次数
    */
    private Long apiCallStats;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
