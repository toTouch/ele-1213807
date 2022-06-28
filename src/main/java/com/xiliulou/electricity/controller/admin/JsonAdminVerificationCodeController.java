package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.VerificationCode;
import com.xiliulou.electricity.query.VerificationCodeQuery;
import com.xiliulou.electricity.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 动态验证码
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-06-28-11:09
 */
@RestController
@RequestMapping("/admin/verification_code/")
public class JsonAdminVerificationCodeController extends BaseController {

    @Autowired
    private VerificationCodeService verificationCodeService;

    /**
     * 生成动态验证码
     *
     * @param entity
     * @return
     */
    @PostMapping("generate")
    public R generationCode(@RequestBody @Validated VerificationCodeQuery entity, BindingResult result) {
        if (result.hasFieldErrors()) {
            return R.fail("SYSTEM.0002", result.getFieldError().getDefaultMessage());
        }

        return verificationCodeService.generationCode(entity);
    }


    /**
     * 分页列表
     *
     * @return
     */
    @GetMapping("page")
    public R page(@RequestParam("size") int size,
                  @RequestParam("offset") int offset,
                  @RequestParam(value = "userName", required = false) String userName,
                  @RequestParam(value = "phone", required = false) String phone,
                  @RequestParam(value = "verificationCode", required = false) String verificationCode) {

        if (size < 0 || size > 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        VerificationCodeQuery codeQuery = VerificationCodeQuery.builder()
                .size(size)
                .offset(offset)
                .userName(userName)
                .phone(phone)
                .verificationCode(verificationCode)
                .build();
        List<VerificationCode> list = verificationCodeService.queryAllByLimit(codeQuery);

        return R.ok(list);
    }

    @GetMapping("page_count")
    public R selectPageCount(@RequestParam("size") int size,
                             @RequestParam("offset") int offset,
                             @RequestParam(value = "userName", required = false) String userName,
                             @RequestParam(value = "phone", required = false) String phone,
                             @RequestParam(value = "verificationCode", required = false) String verificationCode) {

        if (size < 0 || size > 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        VerificationCodeQuery codeQuery = VerificationCodeQuery.builder()
                .size(size)
                .offset(offset)
                .userName(userName)
                .phone(phone)
                .verificationCode(verificationCode)
                .build();

        return R.ok(verificationCodeService.selectCountByQuery(codeQuery));
    }

    /**
     * 逻辑删除验证码
     *
     * @param id
     * @return
     */
    @DeleteMapping("delete/{id}")
    public R delete(@PathVariable("id") Long id) {
        return verificationCodeService.deleteVerificationCode(id);
    }


    @GetMapping("checkCode/{verificationCode}")
    public R checkVerificationCode(@PathVariable("verificationCode") String verificationCode) {
        return verificationCodeService.checkVerificationCode(verificationCode);
    }


}
