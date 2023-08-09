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
     * 租期
     */
    private Integer tenancy;

    /**
     * 租期单位
     */
    private Integer residueUnit;

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
