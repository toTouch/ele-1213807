package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ApiRequestQuery;
import com.xiliulou.electricity.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Miss.Li
 * @Date: 2021/11/5 10:18
 * @Description:
 */
@RestController
public class JsonOuterEleApiController extends BaseController {
	@Autowired
	ApiService apiService;

	@PostMapping("/outer/api/ele")
	public R sendCommand(@RequestBody ApiRequestQuery apiRequestQuery) {
		return returnTripleResult(apiService.handleCommand(apiRequestQuery));
	}
}
