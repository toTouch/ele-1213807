package com.xiliulou.electricity.service.userinfo.overdue;


import com.xiliulou.electricity.query.userinfo.overdue.OverdueRemarkReq;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

/**
 * <p>
 * Description: This interface is UserInfoOverdueRemarkService!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/6/25
 **/
public interface UserInfoOverdueRemarkService {
    
    Triple<Boolean,String, String> insertOrUpdate(OverdueRemarkReq request);
}
