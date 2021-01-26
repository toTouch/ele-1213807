package com.xiliulou.electricity.query;

import com.baomidou.mybatisplus.annotation.TableId;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;
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
    * 门店账号
    */
    @NotEmpty(message = "门店账号不能为空!", groups = {CreateGroup.class})
    private String sn;
    /**
    * 门店名称
    */
    @NotEmpty(message = "门店名称不能为空!", groups = {CreateGroup.class})
    private String name;
    /**
    * 门店地址
    */
    @NotEmpty(message = "门店地址不能为空!", groups = {CreateGroup.class})
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
     * 联系电话
     */
    @NotEmpty(message = "联系电话不能为空!", groups = {CreateGroup.class})
    private String servicePhone;
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

    /**
     * uid
     */
    private Long uid;


}