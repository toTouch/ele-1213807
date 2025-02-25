package com.xiliulou.electricity.bo.merchant;

import com.xiliulou.electricity.enums.YesNoEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantEmployeeBO {
    /**
     * 员工id
     */
    private Long id;

    /**
     * 员工uid
     */
    private Long uid;

    /**
     * 商户uid
     */
    private Long merchantUid;

    /**
     * 0--正常 1--锁住
     */
    private Integer merchantUserLockFlag;


    /**
     * 商户邀请权限：0-开启，1-关闭
     */
    private Integer merchantInviteAuth;

    /**
     * 商户站点代付权限：0-开启，1-关闭
     */
    private Integer merchantEnterprisePackageAuth;

    /**
     * 0--正常 1--锁住
     */
    private Integer employeeUserLockFlag;

    /**
     * 员工邀请权限：0-开启，1-关闭
     */
    private Integer employeeInviteAuth;

    /**
     * 员工站点代付权限：0-开启，1-关闭
     */
    private Integer employeeEnterprisePackageAuth;

    /**
     * 会员代付权限 0：关，1：开
     */
    private Integer purchaseAuthority;

    /**
     * 商户id
     */
    private Long merchantId;

    public Integer getInviteAuth() {
        // 商户邀请权限开启并且员工邀请权限开启 则为开启
        if (Objects.equals(merchantInviteAuth, YesNoEnum.YES.getCode()) && Objects.equals(employeeInviteAuth, YesNoEnum.YES.getCode())) {
            return YesNoEnum.YES.getCode();
        }

        return YesNoEnum.NO.getCode();
    }

    public Integer getEnterprisePackageAuth() {
        // 商户邀请权限开启并且员工邀请权限开启 则为开启
        if (Objects.equals(employeeEnterprisePackageAuth, YesNoEnum.YES.getCode()) && Objects.equals(merchantEnterprisePackageAuth, YesNoEnum.YES.getCode())) {
            return YesNoEnum.YES.getCode();
        }

        return YesNoEnum.NO.getCode();
    }
}
