package com.xiliulou.electricity.controller.user.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.query.installment.InstallmentSignQuery;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.vo.installment.InstallmentRecordVO;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:35
 */
@RestController
@RequestMapping("/user/Installment/record")
@AllArgsConstructor
public class JsonUserInstallmentRecordController {
    
    private InstallmentRecordService installmentRecordService;
    

    
    /**
     * 查询当前登录用户正在使用的签约记录信息
     */
    @ProcessParameter
    @GetMapping("/detail")
    public R<InstallmentRecordVO> queryInstallmentRecordForUser() {
        return installmentRecordService.queryInstallmentRecordForUser();
    }
    

}
