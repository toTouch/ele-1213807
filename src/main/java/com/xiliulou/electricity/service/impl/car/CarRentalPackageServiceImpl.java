package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.mapper.car.CarRentalPackageMapper;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 租车套餐业务接口实现
 *
 * @author xiaohui.song
 **/
@Service
@Slf4j
public class CarRentalPackageServiceImpl implements CarRentalPackageService {

    @Resource
    private CarRentalPackageMapper carRentalPackageMapper;

    /**
     * 检测唯一：租户ID+套餐名称
     *
     * @param tenantId 租户ID
     * @param name     套餐名称
     * @return
     */
    @Override
    public R<Boolean> uqByTenantIdAndName(Integer tenantId, String name) {
        if (null == tenantId || null == name) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        int num = carRentalPackageMapper.uqByTenantIdAndName(tenantId, name);
        return R.ok(num >= 0);
    }

    /**
     * 根据ID修改上下架状态
     *
     * @param id 主键ID
     * @param status 上下架状态
     * @param uid 操作人ID
     * @return
     */
    @Override
    public R<Boolean> updateStatusById(Long id, Integer status, Long uid) {
        if (ObjectUtils.allNotNull(id, status, uid)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        if (!BasicEnum.isExist(status, UpDownEnum.class)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        long updateTime = System.currentTimeMillis();
        int num = carRentalPackageMapper.updateStatusById(id, status, uid, updateTime);
        return R.ok(num >= 0);
    }

    /**
     * 根据ID删除
     *
     * @param id 主键ID
     * @param uid 操作人ID
     * @return
     */
    @Override
    public R<Boolean> delById(Long id, Long uid) {
        if (ObjectUtils.allNotNull(id, uid)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        // TODO 已经产生了套餐订单数据，不允许删除
        long delTime = System.currentTimeMillis();
        int num = carRentalPackageMapper.delById(id, uid, delTime);
        return R.ok(num >= 0);
    }

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return
     */
    @Override
    public R<List<CarRentalPackagePO>> list(CarRentalPackageQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId()) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return R.ok(carRentalPackageMapper.list(qryModel));
    }

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<List<CarRentalPackagePO>> page(CarRentalPackageQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId()) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return R.ok(carRentalPackageMapper.page(qryModel));
    }

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<Integer> count(CarRentalPackageQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId()) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return R.ok(carRentalPackageMapper.count(qryModel));
    }

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return
     */
    @Slave
    @Override
    public R<CarRentalPackagePO> selectById(Long id) {
        if (null == id) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return R.ok(carRentalPackageMapper.selectById(id));
    }

    /**
     * 根据ID修改数据
     * @param optModel 操作模型
     * @return
     */
    @Override
    public R<Boolean> updateById(CarRentalPackageOptModel optModel) {
        if (optModel == null || optModel.getId() <= 0 || optModel.getUpdateUid() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        Integer tenantId = optModel.getTenantId();
        String name = optModel.getName();
        // 检测唯一
        if (carRentalPackageMapper.uqByTenantIdAndName(tenantId, name) > 0) {
            return R.fail("300100", "套餐名称已存在");
        }
        // 检测原始套餐状态
        CarRentalPackagePO oriEntity = carRentalPackageMapper.selectById(optModel.getId());
        if (oriEntity == null || DelFlagEnum.DEL.getCode().equals(oriEntity.getDelFlag())) {
            return R.fail("300101", "套餐不存在");
        }
        if (UpDownEnum.UP.getCode().equals(oriEntity.getStatus())) {
            return R.fail("300102", "上架状态的套餐不允许修改");
        }
        CarRentalPackagePO entity = new CarRentalPackagePO();
        BeanUtils.copyProperties(optModel, entity);
        // 赋值修改时间
        long now = System.currentTimeMillis();
        entity.setUpdateTime(now);
        int num = carRentalPackageMapper.updateById(entity);
        return R.ok(num >= 0);
    }

    /**
     * 新增数据，返回主键ID
     * @param optModel 操作模型
     * @return
     */
    @Override
    public R<Long> insert(CarRentalPackageOptModel optModel) {
        R<Long> checkInsertParamsResult = checkInsertParams(optModel);
        if (!checkInsertParamsResult.isSuccess()) {
            return checkInsertParamsResult;
        }
        Integer tenantId = optModel.getTenantId();
        String name = optModel.getName();
        // 检测唯一
        if (carRentalPackageMapper.uqByTenantIdAndName(tenantId, name) > 0) {
            return R.fail("300100", "套餐名称已存在");
        }
        CarRentalPackagePO entity = new CarRentalPackagePO();
        BeanUtils.copyProperties(optModel, entity);
        // 赋值操作人及时间
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        // 保存入库
        carRentalPackageMapper.insert(entity);
        return R.ok(entity.getId());
    }

    /**
     * 新增校验
     * @param optModel
     */
    private R<Long> checkInsertParams(CarRentalPackageOptModel optModel) {
        if (optModel == null) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        // TODO 明细校验
        return R.ok();
    }

}
