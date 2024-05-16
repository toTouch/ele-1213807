package com.xiliulou.electricity.event.subscriber;


import com.xiliulou.electricity.event.OverdueUserRemarkEvent;
import com.xiliulou.electricity.mapper.userinfo.overdue.UserInfoOverdueRemarkMapper;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class OverdueUserRemarkSubscriber {
    
    private final UserInfoOverdueRemarkMapper userInfoOverdueRemarkMapper;
    
    public OverdueUserRemarkSubscriber(UserInfoOverdueRemarkMapper userInfoOverdueRemarkMapper) {
        this.userInfoOverdueRemarkMapper = userInfoOverdueRemarkMapper;
    }
    
    @EventListener
    public void handleOverdueUserRemarkEvent(OverdueUserRemarkEvent event) {
        if (Objects.isNull(event)){
            log.warn("Received subscription event OverdueUserRemarkEvent is null");
            return;
        }
        
        log.info("Received subscription event OverdueUserRemarkEvent: uid[{}] type[{}]",event.getUid(),event.getType());
        this.userInfoOverdueRemarkMapper.clearRemarksByUidAndType(event.getUid(), event.getType(),event.getTenantId());
    }
}
