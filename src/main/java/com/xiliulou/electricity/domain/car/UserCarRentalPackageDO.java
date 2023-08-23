package com.xiliulou.electricity.domain.car;

import com.xiliulou.electricity.vo.userinfo.UserCarRentalPackageVO;
import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/8/21 20:40
 * @Description:
 */

@Data
public class UserCarRentalPackageDO {
    /**
     * 用户ID
     */
    private Long uid;
    /**
     * 用户名称
     */
    private String name;
    /**
     * 用户电话
     */
    private String phone;
    /**
     * 用户可用状态 0--启用，1--禁用
     */
    private Integer usableStatus;
    /**
     * 套餐ID
     */
    private Long packageId;
    /**
     * 套餐名称
     */
    private String packageName;
    /**
     * 套餐类型
     */
    private Integer packageType;
    /**
     * 当前车辆名称
     */
    private String carName;
    /**
     * 车辆SN
     */
    private String carSn;
    /**
     * 车辆型号
     */
    private String carModel;
    /**
     * 当前电池名称
     */
    private String batteryName;
    /**
     * 套餐状态
     * 0-待生效、1-正常、2-申请冻结、3-冻结、4-申请退押、5-申请退租
     * @see UserCarRentalPackageVO 页面实际显示状态根据VO中的映射为主
     */
    private Integer packageStatus;
    /**
     * 租车押金状态
     * 0-未缴纳 1-已缴纳
     */
    private Integer carDepositStatus;
    /**
     * 车电一体押金缴纳状态
     * 0-已缴纳 1-未缴纳
     */
    private Integer carBatteryDepositStatus;

    /**
     * 套餐到期时间（总到期时间）
     */
    private Long packageExpiredTime;
    /**
     * 增值服务状态
     * 保险状态 3.0重新定义  0：未出险  1：已出险  2：已过期  3：已失效
     */
    private Integer insuranceStatus;
    /**
     * 增值服务到期时间
     */
    private Long insuranceExpiredTime;
    /**
     * 租户ID
     */
    private Long tenantId;
    /**
     * 所属加盟商
     */
    private Long franchiseeId;
    /**
     * 所属加盟商名称
     */
    private String franchiseeName;
    /**
     * 用户认证时间
     */
    private Long userAuthTime;



}
