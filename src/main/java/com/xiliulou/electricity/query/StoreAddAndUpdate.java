package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 门店表(TStore)实体类
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@Data
public class StoreAddAndUpdate {
    /**
    * 门店Id
    */
    @NotNull(message = "门店Id不能为空!", groups = {UpdateGroup.class})
    private Integer id;
    /**
    * 门店名称
    */
    @NotBlank(message = "门店名称不能为空!", groups = {CreateGroup.class,UpdateGroup.class})
    private String name;
    /**
    * 门店地址
    */
    @NotBlank(message = "门店地址不能为空!", groups = {CreateGroup.class})
    private String address;
    /**
    * 地址经度
    */
    @NotNull(message = "地址经度不能为空!", groups = {CreateGroup.class})
    private Double longitude;
    /**
    * 地址纬度
    */
    @NotNull(message = " 地址纬度不能为空!", groups = {CreateGroup.class})
    private Double latitude;
    /**
    * 0--正常 1--删除
    */
    private Object delFlag;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;
    /**
     * 可用状态(0--启用，1--禁用)
     */
    private Integer usableStatus;

    /**
     * 营业时间类型
     */
    @NotEmpty(message = "营业时间类型不能为空!", groups = {CreateGroup.class})
    private String businessTimeType;

    /**
     * 营业开始时间
     */
    private Long beginTime;
    /**
     * 营业结束时间
     */
    private Long endTime;

    @NotBlank(message = "密码不能为空", groups = {CreateGroup.class})
    private String password;
    @NotEmpty(message = "手机号的不能为空", groups = {CreateGroup.class})
    private String servicePhone;

    /**
     * 城市编号
     */
//    @NotNull(message = "城市编号不能为空!", groups = {CreateGroup.class})
    private Integer cityId;

    /**
     * 省编号
     */
    private Integer provinceId;
    /**
     * 区编号
     */
    private Integer regionId;

    /**
     * 加盟商Id
     */
    private Integer franchiseeId;

    /**
     * 缴纳押金方式
     */
    private Integer payType;
    /**
     * 服务类型
     */
    private String serviceType;

    /**
     * 门店详情
     */
    private String detail;

}
