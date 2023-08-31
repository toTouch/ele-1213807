package com.xiliulou.electricity.reqparam.opt.deposit;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 免押订单申请数据模型
 * @author xiaohui.song
 **/
@Data
public class FreeDepositOptReq implements Serializable {

    private static final long serialVersionUID = -7774057774793284895L;

    @NotEmpty(message = "手机号不能为空")
    private String phoneNumber;

    @NotNull(message = "套餐ID不能为空!")
    private Long rentalPackageId;

    @NotEmpty(message = "身份证号不能为空")
    private String idCard;

    @NotEmpty(message = "真实姓名不能为空")
    private String realName;

}
