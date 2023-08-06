package com.xiliulou.electricity.vo.userinfo;

import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.vo.ElectricityUserBatteryVo;
import com.xiliulou.electricity.vo.car.CarRentalPackageDepositPayVo;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderVo;
import com.xiliulou.electricity.vo.car.CarVo;
import com.xiliulou.electricity.vo.insurance.UserInsuranceVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户会员全量信息
 *
 * @author xiaohui.song
 **/
@Data
public class UserMemberInfoVo implements Serializable {

    private static final long serialVersionUID = 7182615524517491404L;

    /**
     * 类型
     * <pre>
     *     1-单车
     *     2-车电一体
     * </pre>
     * @see RentalPackageTypeEnum
     */
    private Integer type;

    /**
     * 到期时间
     */
    private Long dueTime;

    /**
     * 总计到期时间
     */
    private Long dueTimeTotal;

    /**
     * 当前余量
     */
    private Long residue;

    /**
     * 租车套餐名称
     */
    private String rentalPackageName;

    /**
     * 加盟商名称
     */
    private String franchiseeName;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 车辆型号名称
     */
    private String carModelName;

    /**
     * 电池型号名称集
     */
    private List<String> batteryVShortList;

    /**
     * 套餐购买订单信息
     */
    private CarRentalPackageOrderVo carRentalPackageOrder;

    /**
     * 押金缴纳订单信息
     */
    private CarRentalPackageDepositPayVo carRentalPackageDepositPay;

    /**
     * 用户保险信息
     */
    private UserInsuranceVO userInsurance;

    /**
     * 车辆信息
     * */
    private CarVo car;

    /**
     * 电池信息
     */
    private ElectricityUserBatteryVo userBattery;


}
