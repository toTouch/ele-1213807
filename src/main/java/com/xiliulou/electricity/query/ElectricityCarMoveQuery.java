package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zgw
 * @date 2023/3/13 16:25
 * @mood
 */
@Data
public class ElectricityCarMoveQuery {
    
    /**
     * 源门店
     */
    @NotNull(message = "源门店id不能为空!", groups = {UpdateGroup.class})
    private Long sourceSid;
    
    /**
     * 目标门店
     */
    @NotNull(message = "目标门店id不能为空!", groups = {UpdateGroup.class})
    private Long targetSid;
    
    /**
     * 车辆id
     */
    @NotNull(message = "迁移车辆不能为空!", groups = {UpdateGroup.class})
    private List<Long> carIds;
}
