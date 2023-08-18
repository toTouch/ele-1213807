package com.xiliulou.electricity.vo.userinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户单电会员套餐信息
 *
 * @author xiaohui.song
 **/
@Data
public class UserMemberBatteryPackageVo implements Serializable {

    private static final long serialVersionUID = -233405738195860512L;

    /**
     * 到期时间
     */
    private Long dueTime;

    /**
     * 总计到期时间
     */
    private Long dueTimeTotal;

    /**
     * 套餐状态
     */
    private Integer memberCardStatus;
}
