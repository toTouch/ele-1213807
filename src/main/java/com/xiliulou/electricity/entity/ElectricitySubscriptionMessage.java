package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 17:54
 **/
@Data
@TableName("t_electricity_subscription_message")
public class ElectricitySubscriptionMessage extends Model<ElectricitySubscriptionMessage> {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @NotNull(message = "订阅消息类型不能为空!")
    private Integer type;
    @NotEmpty(message = "订阅消息id不能为空!")
    private String messageId;

    private String remark;
    private Long createTime;
    private Long updateTime;

    //租户id
    private Integer tenantId;
}
