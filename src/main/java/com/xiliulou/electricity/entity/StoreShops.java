package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 门店商品表(StoreShops)实体类
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_store_shops")
public class StoreShops {
    /**
    * 门店商品表Id
    */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
    * 门店商品表名称
    */
    private String name;
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

    private BigDecimal price;


    private BigDecimal discountPrice;


    private Integer tenantId;



    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;


}
