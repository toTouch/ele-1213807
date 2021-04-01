package com.xiliulou.electricity.service.impl;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.stereotype.Service;
import java.util.List;

@Service("typeUserSuperService")
public class TypeUserSuperServiceImpl implements UserTypeService {
    @Override
    public List<Integer> getEleIdListByUserType(TokenUser user) {
        return null;
    }
}
