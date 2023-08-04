package com.xiliulou.electricity.vo.userinfo;

import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.vo.ElectricityUserBatteryVo;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderVO;
import com.xiliulou.electricity.vo.car.CarVO;
import com.xiliulou.electricity.vo.insurance.UserInsuranceVO;
import lombok.Data;

import java.io.Serializable;

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
     * @see CarRentalPackageTypeEnum
     */
    private Integer type;

    /**
     * 套餐购买订单信息
     */
    private CarRentalPackageOrderVO carRentalPackageOrder;

    /**
     * 用户保险信息
     */
    private UserInsuranceVO userInsurance;

    /**
     * 车辆信息
     * */
    private CarVO car;

    /**
     * 电池信息
     */
    private ElectricityUserBatteryVo userBattery;


}
