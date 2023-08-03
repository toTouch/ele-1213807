package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Kenneth
 * @Date: 2023/7/31 14:39
 * @Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_share_money_activity_package")
public class ShareMoneyActivityPackage {

    /**
     * 主键
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
     * 邀请返现活动ID
     */
    private Long activityId;
    /**
     * 套餐ID
     */
    private Long packageId;
    /**
     * 套餐类型
     * @see PackageTypeEnum
     */
    private Integer packageType;
    /**
     * 租户ID
     */
    private Long tenantId;
    /**
     * 是否删除（0-正常，1-删除）
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
