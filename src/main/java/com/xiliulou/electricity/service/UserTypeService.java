package com.xiliulou.electricity.service;
import com.xiliulou.security.bean.TokenUser;
import java.util.List;

/**
 * @author: lxc
 * @Date: 2021/3/30 14:08
 * @Description:
 */
public interface UserTypeService {
	List<Integer>  getEleIdListByUserType(TokenUser user);
	List<Integer>  getEleIdListByDataType(TokenUser user);
 
}
