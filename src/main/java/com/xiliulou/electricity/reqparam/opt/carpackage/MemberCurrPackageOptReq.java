package com.xiliulou.electricity.reqparam.opt.carpackage;

import lombok.Data;

import java.io.Serializable;

/**
 * 会员当前套餐操作请求数据
 * @author xiaohui.song
 **/
@Data
public class MemberCurrPackageOptReq implements Serializable {

    private static final long serialVersionUID = 8969242143973747351L;

    /**
     * 套餐购买编码
     */
    private String packageOrderNo;

    /**
     * 租期
     */
    private Integer tenancy;

    /**
     * 到期时间
     */
    private Long dueTime;

    /**
     * 余量(次)
     */
    private Long residue;

    /**
     * 用户UID
     */
    private Long uid;
}
