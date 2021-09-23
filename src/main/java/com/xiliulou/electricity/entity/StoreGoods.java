package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.validator.CreateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
@TableName("t_store_goods")
public class StoreGoods {
    /**
    * 门店商品表Id
    */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    @NotNull(message = "门店不能为空!", groups = {CreateGroup.class})
    private Long storeId;

    /**
    * 门店商品名称
    */
    @NotEmpty(message = "门店商品名称不能为空!", groups = {CreateGroup.class})
    private String name;
    /**
    * 0--正常 1--删除
    */
    private Integer delFlag;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;

    @NotNull(message = "价格不能为空!", groups = {CreateGroup.class})
    private BigDecimal price;

    @NotNull(message = "优惠价格不能为空!", groups = {CreateGroup.class})
    private BigDecimal discountPrice;


    private Integer tenantId;



    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;


}
