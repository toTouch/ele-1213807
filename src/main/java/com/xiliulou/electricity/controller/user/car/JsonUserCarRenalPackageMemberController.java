package com.xiliulou.electricity.controller.user.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.CarRentalPackageMemberTermVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 租车套餐相关的会员期限相关信息Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/car/renalPackage/member")
public class JsonUserCarRenalPackageMemberController {

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    /**
     * 获取会员期限详情
     * @return
     */
    @GetMapping("/queryDetail")
    public R<CarRentalPackageMemberTermVo> queryDetail() {

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, user.getUid());
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            return R.ok();
        }

        CarRentalPackageMemberTermVo memberTermVo = new CarRentalPackageMemberTermVo();
        BeanUtils.copyProperties(memberTermEntity, memberTermVo);

        // 判定是否过期
        if (memberTermEntity.getDueTimeTotal() <= System.currentTimeMillis()) {
            memberTermVo.setStatus(MemberTermStatusEnum.EXPIRE.getCode());
        }
        return R.ok(memberTermVo);
    }

}
