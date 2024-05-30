package com.xiliulou.electricity.query.supper;


import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * Description: This class is UserResouceReq!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/5/20
 **/
@Data
public class UserGrantSourceReq implements Serializable {
    
    List<Integer> tenantIds;
    
    /**
     * <p>
     * Description: 角色类型,不传默认所有
     * <pre>
     *        0 -- 运营商
     *        1 -- 加盟商
     *        2 -- 门店
     *        -1 -- 所有
     *    </pre>
     *
     * @see com.xiliulou.electricity.enums.supper.GrantType
     * </p>
     */
    List<Integer> type;
    
    @NotNull(message = "[sourceIds]不能为空")
    List<Long> sourceIds;
}
