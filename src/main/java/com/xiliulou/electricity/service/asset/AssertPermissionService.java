package com.xiliulou.electricity.service.asset;

import com.xiliulou.security.bean.TokenUser;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

public interface AssertPermissionService {
    
    
    Triple<List<Long>, List<Long>, Boolean> assertPermissionByTriple(TokenUser userInfo);
    
    Pair<Boolean, List<Long>> assertPermissionByPair(TokenUser userInfo);
}
