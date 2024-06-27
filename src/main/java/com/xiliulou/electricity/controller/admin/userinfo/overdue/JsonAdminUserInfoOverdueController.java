package com.xiliulou.electricity.controller.admin.userinfo.overdue;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.userinfo.overdue.OverdueRemarkReq;
import com.xiliulou.electricity.service.userinfo.overdue.UserInfoOverdueRemarkService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * <p>
 * Description: This class is JsonAdminUserInfoOverdueController!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/6/25
 **/
@Slf4j
@RestController
public class JsonAdminUserInfoOverdueController {
    
    private final UserInfoOverdueRemarkService userInfoOverdueRemarkService;
    
    public JsonAdminUserInfoOverdueController(UserInfoOverdueRemarkService userInfoOverdueRemarkService) {
        this.userInfoOverdueRemarkService = userInfoOverdueRemarkService;
    }
    
    @PutMapping("/admin/userInfo/overdue/remark")
    public R<?> saveOrEdit(@RequestBody @Validated OverdueRemarkReq request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Triple<Boolean,String,String> res = userInfoOverdueRemarkService.insertOrUpdate(request);
        if (!res.getLeft()){
            return R.fail(res.getMiddle(),res.getRight());
        }
        return R.ok();
    }
}
