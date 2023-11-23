package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.bytebuddy.implementation.bind.annotation.Empty;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-11-11:50
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatteryModelQuery {
    private Long size;

    private Long offset;

    private Integer tenantId;

    private String batteryType;

    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;

    @NotNull(message = "电池材质不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long mid;

    /**
     * 电池电压
     */
    @NotNull(message = "电池电压不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Range(min = 0, max = 380, message = "电池电压不合法", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer standardV;

    /**
     * 默认充电电压
     */
    @NotNull(message = "默认充电电压不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Range(min = 0, max = 380, message = "默认充电电压不合法", groups = {CreateGroup.class, UpdateGroup.class})
    private Double chargeV;

    /**
     * 电池串数
     */
    @NotNull(message = "电池串数不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Positive(message = "电池串数不合法!", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer number;
    
    /**
     * 品牌名称
     */
    @NotEmpty(message = "品牌名称不能为空!", groups = {CreateGroup.class})
    private String brandName;
    
    /**
     * 电池容量
     */
    @NotNull(message = "电池容量不能为空!", groups = {CreateGroup.class})
    private Integer capacity;
    
    /**
     * 电池接入协议 0：未知 1：铁塔
     */
    @NotNull(message = "电池接入协议不能为空!", groups = {CreateGroup.class})
    private Integer accessProtocol;
    
    /**
     * 电池尺寸
     */
    @NotEmpty(message = "电池尺寸不能为空!", groups = {CreateGroup.class})
    private String batterySize;
    
    /**
     * 电池重量（Kg)
     */
    @NotNull(message = "电池重量不能为空!", groups = {CreateGroup.class})
    private Double weight;
}
