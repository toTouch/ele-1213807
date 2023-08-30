package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Kenneth
 * @Date: 2023/7/15 14:06
 * @Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_car_move_record")
public class CarMoveRecord {

    private Integer id;

    /**
     *租户ID
     */
    private Long tenantId;

    /**
     * 车辆ID
     */
    private Long carId;

    /**
     * 车辆SN码
     */
    private String carSn;

    /**
     * 车辆型号ID
     */
    private Long carModelId;

    /**
     * 旧加盟商ID
     */
    private Long oldFranchiseeId;

    /**
     * 旧门店ID
     */
    private Long oldStoreId;

    /**
     * 新加盟商ID
     */
    private Long newFranchiseeId;

    /**
     * 新门店ID
     */
    private Long newStoreId;

    /**
     * 操作人
     */
    private Long operator;

    /**
     * 是否删除标记（0-正常，1-删除）
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;

}
