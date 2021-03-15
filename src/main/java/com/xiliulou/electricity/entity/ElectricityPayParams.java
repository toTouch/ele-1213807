package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 09:26
 **/
@Data
@TableName("t_electricity_pay_params")
public class ElectricityPayParams extends Model<ElectricityPayParams> {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @NotEmpty(message = "appId不能为空!")
    private String appId;
    @NotEmpty(message = "appSecret不能为空!")
    private String appSecret;
    @NotEmpty(message = "mchId不能为空!")
    private String mchId;
    @NotEmpty(message = "paternerKey不能为空!")
    private String paternerKey;
    /**
     * api证书名称
     */
    private String apiName;

    private Long createTime;
    private Long updateTime;


}
