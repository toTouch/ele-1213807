package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 基础Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
public class JsonAdminBasicController {

    @Resource
    private UserDataScopeService userDataScopeService;;

    /**
     * 检查权限
     * @return
     */
    protected R  checkPermission() {
        // 用户拦截
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        // 加盟商查询
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
        }
        // 判定返回
        if(CollectionUtils.isEmpty(franchiseeIds)){
            return R.ok(Collections.EMPTY_LIST);
        }
        // 门店不可见
        if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
            return R.ok(Collections.EMPTY_LIST);
        }
        return  R.ok(Collections.EMPTY_LIST);
    }

}
