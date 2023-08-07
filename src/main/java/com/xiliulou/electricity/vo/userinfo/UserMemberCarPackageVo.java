package com.xiliulou.electricity.vo.userinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户单车会员套餐信息
 *
 * @author xiaohui.song
 **/
@Data
public class UserMemberCarPackageVo implements Serializable {

    private static final long serialVersionUID = 3126785278131936065L;

    /**
     * 到期时间
     */
    private Long dueTime;

    /**
     * 总计到期时间
     */
    private Long dueTimeTotal;
}
