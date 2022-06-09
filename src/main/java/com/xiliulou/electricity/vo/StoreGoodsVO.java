package com.xiliulou.electricity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 门店商品表(StoreShops)实体类
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@Data
public class StoreGoodsVO {
    private Long id;

    private Long storeId;

    /**
    * 门店商品名称
    */
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

    private BigDecimal price;

    private BigDecimal discountPrice;


    private Integer tenantId;

    /**
     * 车辆型号
     */
    private String carModel;

    List<ElectricityCabinetFile> electricityCabinetFiles;

}
