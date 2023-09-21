package com.xiliulou.electricity.vo.userinfo;

import com.xiliulou.electricity.domain.car.UserCarRentalPackageDO;
import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/8/21 19:25
 * @Description:
 */

@Data
public class UserCarRentalPackageVO {
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
     * 邀请人
     */
    private String inviterUserName;
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
     * 电池SN信息
     */
    private String batterySn;
    /**
     * 电池型号
     */
    private String batteryModel;
    /**
     * 电池租赁状态，0：未租电池，1：已租电池
     */
    private Integer batteryRentStatus;
    /**
     * 套餐冻结状态
     * 0-已冻结、 1-正常 (该值需要通过数据库状态转换为页面显示状态)
     * @see UserCarRentalPackageDO
     */
    private Integer packageFreezeStatus;
    /**
     * 押金状态
     * 0-未缴纳 1-已缴纳
     */
    private Integer depositStatus;
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
