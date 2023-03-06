package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * (FreeDepositData)表实体类
 *
 * @author zzlong
 * @since 2023-02-20 15:46:34
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_free_deposit_data")
public class FreeDepositData {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 免押次数
     */
    private Integer freeDepositCapacity;
    /**
     * 充值时间
     */
    private Long rechargeTime;

    private Integer tenantId;
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
