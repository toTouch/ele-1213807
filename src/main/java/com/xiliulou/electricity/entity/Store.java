package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 门店表(TStore)实体类
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_store")
public class Store {
    /**
     * 门店Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 门店名称
     */
    private String name;
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
    private String servicePhone;
    /**
     * 营业时间
     */
    private String businessTime;

    private Integer tenantId;

    /**
     * uid
     */
    private Long uid;

    private Long franchiseeId;

    /**
     * 门店分润比例
     */
    private Integer percent;

    /**
     * 城市id
     */
    private Integer cid;

    /**
     * 缴纳押金方式 0--线上 1--线下
     */
    private Integer payType;


    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //可用
    public static final Integer STORE_USABLE_STATUS = 0;
    //禁用
    public static final Integer STORE_UN_USABLE_STATUS = 1;

    //支持
    public static final Integer SUPPORT = 0;
    //不支持
    public static final Integer UN_SUPPORT = 1;

}
