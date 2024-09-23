package com.xiliulou.electricity.controller.admin.fy;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.fy.FyConfigRequest;
import com.xiliulou.electricity.service.FyConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.fy.FyConfigVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * <p>
 * Description: This class is JsonAdminFyController!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/8/28
 **/
@RestController
@RequestMapping("/admin/fy")
@AllArgsConstructor
public class JsonAdminFyController {
    
    private final FyConfigService fyConfigService;
    
    @GetMapping("/detail")
    public R<FyConfigVO> queryConfig() {
        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (Objects.isNull(userInfo)){
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!(SecurityUtils.isAdmin() ||
                Objects.equals(userInfo.getDataType(), User.DATA_TYPE_OPERATE) ||
                Objects.equals(userInfo.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
        FyConfig config = fyConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(config)) {
            return R.ok();
        }
        FyConfigVO configVO = new FyConfigVO();
        configVO.setTenantId(config.getTenantId());
        configVO.setStoreCode(config.getStoreCode());
        configVO.setMerchantCode(config.getMerchantCode());
        configVO.setChannelCode(config.getChannelCode());
        return R.ok(configVO);
    }
    
    @PostMapping("/edit")
    public R<?> saveConfig(@RequestBody FyConfigRequest params) {
        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (Objects.isNull(userInfo)){
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!(SecurityUtils.isAdmin() ||
                Objects.equals(userInfo.getDataType(), User.DATA_TYPE_OPERATE) ||
                Objects.equals(userInfo.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
        Pair<Boolean, String> result =  fyConfigService.saveOrUpdate(TenantContextHolder.getTenantId(),params);
        if (!result.getLeft()){
            return R.failMsg(result.getRight());
        }
        return R.ok();
    }
}
