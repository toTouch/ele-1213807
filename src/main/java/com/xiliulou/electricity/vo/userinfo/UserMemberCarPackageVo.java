package com.xiliulou.electricity.vo.userinfo;

import com.xiliulou.electricity.enums.MemberTermStatusEnum;
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

    /**
     * 状态
     * <pre>
     *     0-待生效
     *     1-正常
     *     2-申请冻结
     *     3-冻结
     *     4-申请退押
     *     5-申请退租
     * </pre>
     * @see MemberTermStatusEnum
     */
    private Integer status;
}
