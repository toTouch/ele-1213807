package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.UserMapper;
import com.xiliulou.electricity.mapper.merchant.MerchantMapper;
import com.xiliulou.electricity.request.merchant.MerchantSaveRequest;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2024/2/6 11:10
 * @desc
 */
@Service("merchantService")
@Slf4j
public class MerchantServiceImpl implements MerchantService {
    @Resource
    private MerchantMapper merchantMapper;
    @Resource
    private UserMapper userMapper;
    
    /**
     * 商户保存
     * @param merchantSaveRequest
     * @param uid
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> save(MerchantSaveRequest merchantSaveRequest, Long uid) {
        Integer tenantId = TenantContextHolder.getTenantId();
        // 检测商户名称是否存在
        User user = userMapper.checkMerchantExist(merchantSaveRequest.getName(),merchantSaveRequest.getPhone(), User.TYPE_USER_MERCHANT, tenantId, null);
        if (Objects.nonNull(user) && Objects.equals(user.getName(), merchantSaveRequest.getName())) {
            return Triple.of(false,"","商户名称已经存在");
        }
        
        // 检测手机号
        if (Objects.nonNull(user) && Objects.equals(user.getPhone(), merchantSaveRequest.getPhone())) {
            return Triple.of(false,"","商户名称已经存在");
        }
        
        return null;
    }
}
