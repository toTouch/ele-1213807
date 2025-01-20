package com.xiliulou.electricity.vo.userinfo;

import com.xiliulou.electricity.domain.car.UserCarRentalPackageDO;
import lombok.Data;

/**
 * @author HeYafeng
 * @date 2025/1/3 19:37:49
 */
@Data
public class UserCarRentalPackageProVO {
    
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
     * 套餐冻结状态 0-已冻结、 1-正常 (该值需要通过数据库状态转换为页面显示状态)
     *
     * @see UserCarRentalPackageDO
     */
    private Integer packageFreezeStatus;
    
    /**
     * 押金状态 0-未缴纳 1-已缴纳(实缴) -- 2 已缴纳(免押)
     */
    private Integer depositStatus;
    
    /**
     * 套餐到期时间（总到期时间）
     */
    private Long packageExpiredTime;
    
    /**
     * 所属加盟商
     */
    private Long franchiseeId;
    
    /**
     * 所属加盟商名称
     */
    private String franchiseeName;
    
    /**
     * 所属门店
     */
    private Long storeId;
    
    /**
     * 所属加盟商名称
     */
    private String storeName;
    
    UserBasicInfoCarProVO basicInfo;
    
    /**
     * 用户会员全量信息
     */
    private UserMemberInfoVo userMemberInfoVo;
    
    public static Integer FREE_OF_CHARGE = 2;
    
    public static Integer PAID_IN = 1;
    
    public static Integer UNPAID = 0;
}
