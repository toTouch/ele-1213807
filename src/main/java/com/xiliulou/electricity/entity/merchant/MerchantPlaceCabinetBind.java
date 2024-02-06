package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/2/6 10:54
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_place_cabinet_bind")
public class MerchantPlaceCabinetBind {
    /**
     *  主键ID
     */
    private Long id;
    /**
     *  场地id
     */
    private Long placeId;
    /**
     *  柜机id
     */
    private Long cabinetId;
    /**
     *  绑定时间
     */
    private Long bindTime;
    /**
     *  解绑时间
     */
    private Long unBindTime;
    /**
     *  状态(0-解绑，1-绑定)
     */
    private Integer status;
    /**
     *  场地费补日结算标记(0-否，1-是)
     */
    private Integer placeDailySettlement;
    /**
     *  电费月结算标记(0-否，1-是)
     */
    private Integer monthSettlement;
    /**
     *  电费月结算详情(为json数组记录具体的年月)
     */
    private String monthSettlementDetail;
    /**
     *  场地月结算详情(为json数组记录具体的年月)
     */
    private Integer placeMonthSettlement;
    /**
     *  场地月结算标记(0-否，1-是)
     */
    private String placeMonthSettlementDetail;
    /**
     *  月结算详情(为json数组记录具体的年月)
     */
    private Integer tenantId;
    /**
     * 删除标记(0-未删除，1-已删除)
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
