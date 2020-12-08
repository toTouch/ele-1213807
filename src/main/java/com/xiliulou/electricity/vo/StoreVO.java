package com.xiliulou.electricity.vo;

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

/**
 * 门店表(TStore)实体类
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@Data
public class StoreVO {

    private Integer id;
    /**
    * 门店账号
    */
    private String sn;
    /**
    * 门店名称
    */
    private String name;
    /**
    * 门店地区Id
    */
    private Integer areaId;
    /**
    * 门店地址
    */
    private String address;
    /**
    * 地址经度
    */
    private Double longitude;
    /**
    * 地址纬度
    */
    private Double latitude;
    /**
    * 电池库存
    */
    private Integer batteryStock;
    /**
    * 租电池服务(0--支持，1--不支持)
    */
    private Integer batteryService;
    /**
    * 租车服务(0--支持，1--不支持)
    */
    private Integer carService;
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
     * 门店地区
     */
    private String areaName;

    /**
     * 电池在用数量
     */
    private Integer useStock;

    private Integer pid;
    /**
     * 可用状态(0--启用，1--禁用)
     */
    private Integer usableStatus;

    private Double distance;
    /**
     * 联系电话
     */
    private String servicePhone;
    /**
     * 营业时间
     */
    private String businessTime;
    /**
     * 营业时间类型
     */
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
     * 是否营业 0--营业 1--打烊
     */
    private Integer isBusiness;

    //全天
    public static final String ALL_DAY = "-1";
    //自定义时间段
    public static final String CUSTOMIZE_TIME = "1";
    //不合法数据
    public static final String ILLEGAL_DATA = "2";


}