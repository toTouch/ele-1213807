package com.xiliulou.electricity.controller.user.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.validator.CreateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/18 15:52
 */

@RestController
@Slf4j
public class JsonUserEnterprisePackageController extends BaseController {

    @Resource
    private EnterpriseChannelUserService enterpriseChannelUserService;

    @PostMapping("/user/enterprise/addUser")
    public R addUser(@RequestBody @Validated(CreateGroup.class) EnterpriseChannelUserQuery query) {

       return R.ok(enterpriseChannelUserService.save(query));

    }

    /**
     * 根据手机号查询当前加盟商下的企业渠道用户信息
     * @param phone
     * @param franchiseeId
     * @return
     */
    @GetMapping("/user/enterprise/queryUser")
    public R queryUser(@RequestParam(value = "phone", required = true) String phone,
                       @RequestParam(value = "franchiseeId", required = true) Long franchiseeId) {

        EnterpriseChannelUserQuery enterpriseChannelUserQuery = EnterpriseChannelUserQuery.builder()
                .phone(phone)
                .franchiseeId(franchiseeId)
                .build();

        return returnTripleResult(enterpriseChannelUserService.queryUser(enterpriseChannelUserQuery));
    }

    /**
     * 企业生成二维码前，创建用户邀请记录
     * @param enterpriseId
     * @param franchiseeId
     * @return
     */
    @GetMapping("/user/enterprise/generateEnterpriseUser")
    public R generateUserRecord(@RequestParam(value = "enterpriseId", required = true) Long enterpriseId,
                       @RequestParam(value = "franchiseeId", required = true) Long franchiseeId) {

        EnterpriseChannelUserQuery enterpriseChannelUserQuery = EnterpriseChannelUserQuery.builder()
                .enterpriseId(enterpriseId)
                .franchiseeId(franchiseeId)
                .build();

        return  returnTripleResult(enterpriseChannelUserService.generateChannelUser(enterpriseChannelUserQuery));
    }

    /**
     * 被邀请用户扫码后，将该用户添加至关联的企业中
     * @param id
     * @param uid
     * @param renewalStatus
     * @return
     */
    @GetMapping("/user/enterprise/addUserByScan")
    public R addUserByScan(@RequestParam(value = "id", required = true) Long id,
                           @RequestParam(value = "uid", required = true) Long uid,
                           @RequestParam(value = "renewalStatus", required = true) Integer renewalStatus) {

        EnterpriseChannelUserQuery enterpriseChannelUserQuery = EnterpriseChannelUserQuery.builder()
                .id(id)
                .uid(uid)
                .renewalStatus(renewalStatus)
                .build();

        return  returnTripleResult(enterpriseChannelUserService.updateUserAfterQRScan(enterpriseChannelUserQuery));
    }

    @GetMapping("/user/enterprise/checkChannelUser")
    public R checkChannelUser(@RequestParam(value = "id", required = true) Long id,
                              @RequestParam(value = "uid", required = true) Long uid) {

        return returnTripleResult(enterpriseChannelUserService.checkUserExist(id, uid));
    }




}
