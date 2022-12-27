package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@Data
@Builder
public class UserRentCarOrderQuery {
    @NotBlank(message = "车辆编码不能为空!")
    private String sn;

    private Long size;
    private Long offset;
    /**
     * 用户名字
     */
    private String name;
    private String phone;
    /**
     * 门店Id
     */
    private Integer carStoreId;
    private Long beginTime;
    private Long endTime;
    private Integer status;
}
