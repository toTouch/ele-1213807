package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.ShareMoneyActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.ShareMoneyActivityQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.ShareMoneyActivityService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 活动表(TActivity)表控制层
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@RestController
@Slf4j
public class JsonAdminShareMoneyActivityController {
	/**
	 * 服务对象
	 */
	@Autowired
	private ShareMoneyActivityService shareMoneyActivityService;

	@Autowired
	FranchiseeService franchiseeService;

	@Autowired
	BatteryMemberCardService batteryMemberCardService;

	@Autowired
	private CarRentalPackageService carRentalPackageService;

	@GetMapping(value = "/admin/shareMoneyActivity/checkActivityStatusOn")
	public R checkActivityStatusOn() {
		return shareMoneyActivityService.checkActivityStatusOn();
	}

	//新增
	@PostMapping(value = "/admin/shareMoneyActivity")
	public R save(@RequestBody @Validated(value = CreateGroup.class) ShareMoneyActivityAddAndUpdateQuery shareMoneyActivityAddAndUpdateQuery) {
		return shareMoneyActivityService.insert(shareMoneyActivityAddAndUpdateQuery);
	}

	//修改--暂时无此功能
	@PutMapping(value = "/admin/shareMoneyActivity")
	public R update(@RequestBody @Validated(value = UpdateGroup.class) ShareMoneyActivityAddAndUpdateQuery shareMoneyActivityAddAndUpdateQuery) {
		return shareMoneyActivityService.update(shareMoneyActivityAddAndUpdateQuery);
	}

	//列表查询
	@GetMapping(value = "/admin/shareMoneyActivity/list")
	public R queryList(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "status", required = false) Integer status) {
		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();


		ShareMoneyActivityQuery shareMoneyActivityQuery = ShareMoneyActivityQuery.builder()
				.offset(offset)
				.size(size)
				.name(name)
				.tenantId(tenantId)
				.status(status).build();

		return shareMoneyActivityService.queryList(shareMoneyActivityQuery);
	}


	//列表查询
	@GetMapping(value = "/admin/shareMoneyActivity/count")
	public R queryCount(@RequestParam(value = "name", required = false) String name,
						@RequestParam(value = "status", required = false) Integer status) {

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();


		ShareMoneyActivityQuery shareMoneyActivityQuery = ShareMoneyActivityQuery.builder()
				.name(name)
				.tenantId(tenantId)
				.status(status).build();

		return shareMoneyActivityService.queryCount(shareMoneyActivityQuery);
	}


	//根据id查询活动详情
	@GetMapping(value = "/admin/shareMoneyActivity/queryInfo/{id}")
	public R queryInfo(@PathVariable("id") Integer id) {
		if (Objects.isNull(id)) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}
		return shareMoneyActivityService.queryInfo(id);
	}

	@GetMapping(value = "/admin/shareMoneyActivity/queryPackages")
	public R queryPackagesByFranchisee(@RequestParam(value = "offset") Long offset,
									   @RequestParam(value = "size") Long size,
									   @RequestParam(value = "type",  required = true) Integer type) {

		List<Integer> packageTypes = Arrays.stream(PackageTypeEnum.values()).map(PackageTypeEnum::getCode).collect(Collectors.toList());
		if(!packageTypes.contains(type)){
			return R.fail("000200", "业务类型参数不合法");
		}

		//需要获取租金不可退的套餐
		if(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(type)){
			BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
					.offset(offset)
					.size(size)
					.delFlag(BatteryMemberCard.DEL_NORMAL)
					.status(BatteryMemberCard.STATUS_UP)
					.isRefund(BatteryMemberCard.NO)
					.tenantId(TenantContextHolder.getTenantId()).build();
			return R.ok(batteryMemberCardService.selectByQuery(query));
		}else{
			CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
			qryModel.setOffset(offset.intValue());
			qryModel.setSize(size.intValue());
			qryModel.setTenantId(TenantContextHolder.getTenantId());
			qryModel.setStatus(UpDownEnum.UP.getCode());
			qryModel.setRentRebate(YesNoEnum.NO.getCode());

			if(PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode().equals(type)){
				qryModel.setType(RentalPackageTypeEnum.CAR_BATTERY.getCode());
			}else if(PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode().equals(type)){
				qryModel.setType(RentalPackageTypeEnum.CAR.getCode());
			}

			return R.ok(batteryMemberCardService.selectCarRentalAndElectricityPackages(qryModel));
		}

	}

	@GetMapping(value = "/admin/shareMoneyActivity/queryPackagesCount")
	public R getElectricityUsablePackageCount(@RequestParam(value = "type",  required = true) Integer type) {

		List<Integer> packageTypes = Arrays.stream(PackageTypeEnum.values()).map(PackageTypeEnum::getCode).collect(Collectors.toList());
		if(!packageTypes.contains(type)){
			return R.fail("000200", "业务类型参数不合法");
		}

		//需要获取租金不可退的套餐
		if(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(type)){
			BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
					.delFlag(BatteryMemberCard.DEL_NORMAL)
					.status(BatteryMemberCard.STATUS_UP)
					.isRefund(BatteryMemberCard.NO)
					.tenantId(TenantContextHolder.getTenantId()).build();
			return R.ok(batteryMemberCardService.selectByPageCount(query));
		}else{
			CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
			qryModel.setTenantId(TenantContextHolder.getTenantId());
			qryModel.setStatus(UpDownEnum.UP.getCode());
			qryModel.setRentRebate(YesNoEnum.NO.getCode());

			if(PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode().equals(type)){
				qryModel.setType(RentalPackageTypeEnum.CAR_BATTERY.getCode());
			}else if(PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode().equals(type)){
				qryModel.setType(RentalPackageTypeEnum.CAR.getCode());
			}
			return R.ok(carRentalPackageService.count(qryModel));
		}
	}

}
