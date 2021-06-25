package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.Role;
import com.xiliulou.electricity.entity.UserRole;
import java.util.List;

/**
 * (UserRole)表服务接口
 *
 * @author makejava
 * @since 2020-12-09 14:19:42
 */
public interface UserRoleService {

    /**
     * 新增数据
     *
     * @param userRole 实例对象
     * @return 实例对象
     */
    UserRole insert(UserRole userRole);


	List<Role> queryByUid(Long uid);

	boolean existsRole(Long id);

    boolean deleteByUid(Long uid);
}
