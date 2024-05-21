package com.xiliulou.electricity.query.supper;


import lombok.Data;

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
public class UserSourceReq implements Serializable {
    List<Long> tenantIds;
    Long sourceId;
}
