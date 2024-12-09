package com.xiliulou.electricity.service.impl.lostuser;

import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.lostuser.LostUserRecord;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.mapper.UserInfoExtraMapper;
import com.xiliulou.electricity.mapper.lostuser.LostUserRecordMapper;
import com.xiliulou.electricity.service.lostuser.LostUserRecordService;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


/**
 * 流失用户记录(LostUserRecord)表服务实现类
 *
 * @author maxiaodong
 * @since 2024-10-09 11:39:09
 */

@Service("lostUserRecordService")
@Slf4j
public class LostUserRecordServiceImpl implements LostUserRecordService {
    @Resource
    private LostUserRecordMapper lostUserRecordMapper;
    
    @Resource
    private UserInfoExtraMapper userInfoExtraMapper;
    
    
    @Override
    @Transactional
    public void doLostUser(TokenUser user, UserInfoExtra userInfoExtra, long prospectTime, Integer packageType, String orderId) {
        long currentTimeMillis = System.currentTimeMillis();
        // 修改用户为流失用户
        UserInfoExtra userInfoExtraUpdate = UserInfoExtra.builder().lostUserStatus(YesNoEnum.YES.getCode()).lostUserTime(currentTimeMillis).updateTime(currentTimeMillis)
                .uid(user.getUid()).build();
        userInfoExtraMapper.updateByUid(userInfoExtraUpdate);
        
        // 保存流失用户的修改记录
        LostUserRecord lostUserRecord = new LostUserRecord();
        lostUserRecord.setUid(user.getUid());
        lostUserRecord.setActualTime(currentTimeMillis);
        lostUserRecord.setOrderId(orderId);
        lostUserRecord.setPackageType(packageType);
        lostUserRecord.setProspectTime(prospectTime);
        lostUserRecord.setTenantId(user.getTenantId());
        lostUserRecord.setCreateTime(currentTimeMillis);
        lostUserRecord.setUpdateTime(currentTimeMillis);
        lostUserRecordMapper.insert(lostUserRecord);
    }
}
