package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.SignFileQuery;
import com.xiliulou.electricity.service.EleCabinetSignatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = "/user/createSignFile")
    public R createSignFile(){
        return returnTripleResult(eleCabinetSignatureService.createFileByTemplate());
    }

    @PostMapping(value = "/user/fileSignature")
    public R fileSignature(@RequestBody @Validated SignFileQuery signFileQuery){
        return returnTripleResult(eleCabinetSignatureService.getSignFlowLink(signFileQuery));
    }

    @GetMapping(value = "/user/signFlowLink/{signFlowId}")
    public R getSignatureLink(@PathVariable("signFlowId") String signFlowId){
        return returnTripleResult(eleCabinetSignatureService.getSignFlowUrl(signFlowId));
    }

}
