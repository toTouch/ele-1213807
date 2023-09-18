package com.xiliulou.electricity.vo.userinfo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/8/22 14:57
 * @Description:
 */

@Data
public class UserCarRentalInfoExcelVO {

    @ExcelProperty("用户名")
    private String userName;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("套餐类型")
    private String packageType;

    @ExcelProperty("当前车辆")
    private String currentCar;

    @ExcelProperty("当前电池")
    private String currentBattery;

    @ExcelProperty("套餐名称")
    private String packageName;

    @ExcelProperty("套餐冻结状态")
    private String packageFreezeStatus;

    @ExcelProperty("押金状态")
    private String depositStatus;

    @ExcelProperty("套餐到期时间")
    private String packageExpiredTime;

    @ExcelProperty("增值服务状态")
    private String insuranceStatus;

    @ExcelProperty("增值服务到期时间")
    private String insuranceExpiredTime;

    @ExcelProperty("所属加盟商")
    private String franchiseeName;

    @ExcelProperty("认证时间")
    private String userAuthTime;

}
