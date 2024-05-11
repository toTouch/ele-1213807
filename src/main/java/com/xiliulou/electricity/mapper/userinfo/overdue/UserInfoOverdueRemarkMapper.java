package com.xiliulou.electricity.mapper.userinfo.overdue;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Description: This interface is UserInfoOverdueRemarkMapper!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/5/11
 **/
@Mapper
public interface UserInfoOverdueRemarkMapper {
    
    int clearRemarksByUidAndType(@Param("uid") Long uid, @Param("type") Integer type,@Param("tenantId") Integer tenantId);

}
