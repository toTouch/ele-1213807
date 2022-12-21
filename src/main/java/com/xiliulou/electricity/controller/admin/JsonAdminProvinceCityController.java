package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.CityService;
import com.xiliulou.electricity.service.ProvinceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: eclair
 * @Date: 2021/1/21 18:56
 * @Description:
 */
@RestController
public class JsonAdminProvinceCityController {
	@Autowired
	CityService cityService;
	@Autowired
	ProvinceService provinceService;

	@GetMapping(value="/admin/city/{pid}")
	public R getCityList(@PathVariable("pid")Integer pid) {
		return R.ok(cityService.queryCityListByPid(pid));
	}

	@GetMapping(value="/admin/province/list")
	public R getProvinceList() {
		return R.ok(provinceService.queryList());
	}

	@GetMapping(value="/admin/province/test")
	public void test() throws Exception {
		provinceService.test();
	}

}
