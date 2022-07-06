package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 换电柜表(TElectricityCar)实体类
 *
 * @author makejava
 * @since 2022-06-06 11:00:14
 */
@Data
public class ElectricityCarAddAndUpdate {
    /**
     * 车辆Id
     */
    @NotNull(message = "车辆Id不能为空!", groups = {UpdateGroup.class})
    private Integer id;
    /**
     * 车辆sn
     */
    @NotEmpty(message = "车辆sn不能为空!", groups = {CreateGroup.class})
    private String sn;

    private Integer status;

    /**
     * 型号Id
     */
    @NotNull(message = "型号Id不能为空!", groups = {CreateGroup.class})
    private Integer modelId;

    /**
     * 门店Id
     */
    @NotNull(message = "门店不能为空!", groups = {CreateGroup.class})
    private Integer storeId;

    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;


}
