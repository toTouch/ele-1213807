package com.xiliulou.electricity.tx.userinfo.overdue;


import com.xiliulou.electricity.entity.userinfo.overdue.UserInfoOverdueRemark;
import com.xiliulou.electricity.mapper.userinfo.overdue.UserInfoOverdueRemarkMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * <p>
 * Description: This class is UserInfoOverdueTxService!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/6/25
 **/
@Component
public class UserInfoOverdueTxService {
    private final UserInfoOverdueRemarkMapper userInfoOverdueRemarkMapper;
    
    public UserInfoOverdueTxService(UserInfoOverdueRemarkMapper userInfoOverdueRemarkMapper) {
        this.userInfoOverdueRemarkMapper = userInfoOverdueRemarkMapper;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void insertOrUpdate(UserInfoOverdueRemark remark) {
        Long id = userInfoOverdueRemarkMapper.queryIdByUidAndType(remark.getUid(), remark.getType(), remark.getTenantId());
        if (Objects.isNull(id)){
            remark.setDelFlag(0);
            userInfoOverdueRemarkMapper.insertRemark(remark);
            return;
        }
        
        remark.setId(id);
        userInfoOverdueRemarkMapper.updateRemark(remark);
    }
}
