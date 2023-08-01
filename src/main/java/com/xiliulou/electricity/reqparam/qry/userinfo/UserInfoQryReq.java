package com.xiliulou.electricity.reqparam.qry.userinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户查询条件
 * @author xiaohui.song
 **/
@Data
public class UserInfoQryReq implements Serializable {

    private static final long serialVersionUID = 6996504825364565582L;

    /**
     * 偏移量
     */
    private Integer offset = 0;

    /**
     * 取值数量
     */
    private Integer size = 10;

    /**
     * 关键字查询，手机号/真实姓名，模糊查询
     */
    private String keywords;
}
