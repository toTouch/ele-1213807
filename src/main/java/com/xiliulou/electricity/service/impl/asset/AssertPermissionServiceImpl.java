package com.xiliulou.electricity.service.impl.asset;

import cn.hutool.core.collection.CollUtil;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.security.bean.TokenUser;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName: AssertPermissionServiceImpl
 * @description: 权限
 * @author: renhang
 * @create: 2024-03-18 17:06
 */
@Service
public class AssertPermissionServiceImpl implements AssertPermissionService {
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    @Override
    public Triple<List<Long>, List<Long>, Boolean> assertPermissionByTriple(TokenUser userInfo) {
        List<Long> franchiseeIds = null;
        if (Objects.equals(userInfo.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(userInfo.getUid());
            if (CollUtil.isEmpty(franchiseeIds)) {
                return Triple.of(null, null, false);
            }
        }
        List<Long> storeIds = null;
        if (Objects.equals(userInfo.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(userInfo.getUid());
            if (CollUtil.isEmpty(storeIds)) {
                return Triple.of(null, null, false);
            }
        }
        return Triple.of(franchiseeIds, storeIds, true);
    }
    
    @Override
    public Pair<Boolean, List<Long>> assertPermissionByPair(TokenUser userInfo) {
        List<Long> franchiseeIds = null;
        if (Objects.equals(userInfo.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(userInfo.getUid());
            if (CollUtil.isEmpty(franchiseeIds)) {
                return Pair.of(false, null);
            }
        }
        return Pair.of(true, franchiseeIds);
    }
}
