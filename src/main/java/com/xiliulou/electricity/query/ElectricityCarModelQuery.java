package com.xiliulou.electricity.query;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author: eclair
 * @Date: 2022/6/6 10:02
 * @Description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ElectricityCarModelQuery {
    /**
     * 车辆型号Id
     */
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Integer id;

    /**
     * 型号名称
     */
    @NotBlank(message = "型号名称不能为空", groups = {CreateGroup.class})
    private String name;
    /**
     * 门店Id
     */
    @NotNull(message = "门店不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long storeId;
    /**
     * 加盟商Id
     */
    @NotNull(message = "加盟商不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long franchiseeId;

    /**
     * 租赁方式
     */
    @NotBlank(message = "租赁方式不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String rentType;

    /**
     * 租赁周期
     */
    //private Integer rentTime;
    /**
     * 租车押金
     */
    @NotNull(message = "租车押金不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private BigDecimal carDeposit;

    /**
     * 车辆型号标签
     */
    private String carModelTag;

    /**
     * 其它参数
     */
    private String otherProperties;


    private Long size;
    private Long offset;
    private List<Long> franchiseeIds;
    private List<Long> storeIds;
    private Long uid;
    //租户id
    private Integer tenantId;
}
