package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.EleCabinetSignatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Kenneth
 * @Date: 2023/7/5 8:45
 * @Description:
 */

@RestController
@Slf4j
public class JsonUserElectronicSignatureController extends BaseController {

    @Autowired
    private EleCabinetSignatureService eleCabinetSignatureService;

    @GetMapping(value = "/user/checkEsignStatus")
    public R checkEsignStatus(){
        return returnTripleResult(eleCabinetSignatureService.checkUserEsignFinished());
    }

    @Deprecated
    @GetMapping(value = "/user/identifyAuth")
    public R identifyAuth(){
        return returnTripleResult(eleCabinetSignatureService.personalAuthentication());
    }

    @GetMapping(value = "/user/fileSignature")
    public R fileSignature(){
        return returnTripleResult(eleCabinetSignatureService.getSignFlowLink());
    }

    @GetMapping(value = "/user/querySignFile/{signFlowId}")
    public R querySignFile(@PathVariable("signFlowId") String signFlowId){
        return returnTripleResult(eleCabinetSignatureService.getSignatureFile(signFlowId));
    }

}
