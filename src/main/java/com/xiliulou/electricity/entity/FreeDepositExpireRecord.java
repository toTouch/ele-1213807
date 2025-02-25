package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: FreeDepositExpireRecord
 * @Author: RenHang
 * @Date: 2025/02/25
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_free_deposit_expire_record")
public class FreeDepositExpireRecord {

    private Long id;

    private Long uid;

    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 支付宝绑定的手机号
     */
    private String phone;

    /**
     * 押金类型 1：电池，2：租车
     */
    private Integer depositType;

    /**
     * 免押金额
     */
    private Double transAmt;


    private Integer tenantId;
    /**
     * 加盟商id
     */
    private Long franchiseeId;

    /**
     * 门店Id
     */
    private Long storeId;


    /**
     * 免押时间
     */
    private Long depositTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态: 1:即将到期 2:已到期 3:已处理
     */
    private Integer status;

    private Long createTime;

    private Long updateTime;


}
