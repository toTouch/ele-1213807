package com.xiliulou.electricity.event.subscriber;


import com.xiliulou.electricity.event.OverdueUserRemarkEvent;
import com.xiliulou.electricity.mapper.userinfo.overdue.UserInfoOverdueRemarkMapper;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * <p>
 * Description: This class is OverdueUserRemarkSubscriber!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/5/11
 **/
@Component
public class OverdueUserRemarkSubscriber {
    
    private final UserInfoOverdueRemarkMapper userInfoOverdueRemarkMapper;
    
    public OverdueUserRemarkSubscriber(UserInfoOverdueRemarkMapper userInfoOverdueRemarkMapper) {
        this.userInfoOverdueRemarkMapper = userInfoOverdueRemarkMapper;
    }
    
    @EventListener
    public void handleOverdueUserRemarkEvent(OverdueUserRemarkEvent event) {
        if (Objects.isNull(event)){
            return;
        }
        this.userInfoOverdueRemarkMapper.clearRemarksByUidAndType(event.getUid(), event.getType(),event.getTenantId());
    }
}
