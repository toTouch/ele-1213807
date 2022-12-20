package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CarMemberCardOrderQuery {

    /**
     * 车辆型号
     */
    @NotNull(message = "车辆型号不能为空!")
    private Integer carModelId;
    /**
     * 租赁方式  WEEK_RENT：周租  ，MONTH_RENT：月租
     */
    @NotBlank(message = "租赁方式不能为空!")
    private String rentType;
    /**
     * 租赁周期
     */
    @NotNull(message = "租赁周期不能为空!")
    private Integer rentTime;


    /**
     * 活动id
     */
    private Integer activityId;

    private Integer isBindActivity;
}
