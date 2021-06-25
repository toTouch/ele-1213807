package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;

/**
 * @author lxc
 * @date 2021/06/23 0029 17:01
 * @Description:
 */
@Data
@Builder
public class TenantQuery {

    private Long  size;
    private Long offset;
    private Integer tenantId;
    private  String name;
    private  String code;
    private  Integer status;
    private  Long beginTime;
    private  Long endTime;
}
