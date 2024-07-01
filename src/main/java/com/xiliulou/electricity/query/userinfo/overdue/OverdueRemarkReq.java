package com.xiliulou.electricity.query.userinfo.overdue;


import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * <p>
 * Description: This class is OverdueRemarkReq!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 * @see com.xiliulou.electricity.entity.userinfo.overdue.UserInfoOverdueRemark
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/6/25
 **/
@Data
public class OverdueRemarkReq implements Serializable {
    
    private static final long serialVersionUID = 7701667963518778408L;
    
    @NotNull(message = "[用户]不能为空")
    private Long uid;
    
    @NotNull(message = "[类型]不能为空")
    private Integer type;
    
    private String remark;
}
