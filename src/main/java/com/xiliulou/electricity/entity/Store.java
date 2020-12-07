package com.xiliulou.electricity.entity;
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
    @TableId
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
    * 换电柜地址
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
    private Object batteryService;
    /**
    * 租车服务(0--支持，1--不支持)
    */
    private Object carService;
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

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}