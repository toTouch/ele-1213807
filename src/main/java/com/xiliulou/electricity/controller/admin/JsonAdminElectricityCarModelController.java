package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.query.ElectricityCabinetModelQuery;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 换电柜型号表(TElectricityCarModel)表控制层
 *
 * @author makejava
 * @since 2022-06-06 16:31:04
 */
@RestController
public class JsonAdminElectricityCarModelController {
	/**
	 * 服务对象
	 */
	@Autowired
	ElectricityCarModelService electricityCarModelService;

	//新增换电柜车辆型号
	@PostMapping(value = "/admin/electricityCarModel")
	public R save(@RequestBody @Validated ElectricityCarModel electricityCarModel) {
		return electricityCarModelService.save(electricityCarModel);
	}

	//修改换电柜车辆型号
	@PutMapping(value = "/admin/electricityCarModel")
	public R update(@RequestBody ElectricityCarModel electricityCarModel) {
		return electricityCarModelService.edit(electricityCarModel);
	}

	//删除换电柜型号
	@DeleteMapping(value = "/admin/electricityCarModel/{id}")
	public R delete(@PathVariable("id") Integer id) {
		if (Objects.isNull(id)) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}
		return electricityCarModelService.delete(id);
	}

	//列表查询
	@GetMapping(value = "/admin/electricityCarModel/list")
	public R queryList(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam(value = "name", required = false) String name) {
		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		Integer tenantId = TenantContextHolder.getTenantId();

		ElectricityCarModelQuery electricityCarModelQuery = ElectricityCarModelQuery.builder()
				.offset(offset)
				.size(size)
				.name(name)
				.tenantId(tenantId).build();

		return electricityCarModelService.queryList(electricityCarModelQuery);
	}

	//列表查询
	@GetMapping(value = "/admin/electricityCarModel/queryCount")
	public R queryCount(@RequestParam(value = "name", required = false) String name) {

		Integer tenantId = TenantContextHolder.getTenantId();

		ElectricityCarModelQuery electricityCarModelQuery = ElectricityCarModelQuery.builder()
				.name(name)
				.tenantId(tenantId).build();

		return electricityCarModelService.queryCount(electricityCarModelQuery);
	}


}
