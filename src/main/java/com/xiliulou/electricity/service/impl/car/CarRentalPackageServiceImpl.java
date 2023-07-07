package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CarRenalCacheConstant;
import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.mapper.car.CarRentalPackageMapper;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 租车套餐表 ServiceImpl
 *
 * @author xiaohui.song
 **/
@Service
@Slf4j
public class CarRentalPackageServiceImpl implements CarRentalPackageService {

    @Resource
    private RedisService redisService;

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

    @Resource
    private CarRentalPackageMapper carRentalPackageMapper;

    /**
     * 根据条件查询<br />
     * PS：<br />
     * 1、不区分租户<br />
     * 2、不区分删除<br />
     *
     * @param qryModel
     * @return
     */
    @Override
    public R<List<CarRentalPackagePO>> listByCondition(CarRentalPackageQryModel qryModel) {
        return null;
    }

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
        if (!ObjectUtils.allNotNull(id, status, uid)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        if (!BasicEnum.isExist(status, UpDownEnum.class)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 操作 DB
        long updateTime = System.currentTimeMillis();
        int num = carRentalPackageMapper.updateStatusById(id, status, uid, updateTime);

        // 删除缓存
        delCache(String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_ID_KEY, id));

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
        if (!ObjectUtils.allNotNull(id, uid)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 校验能否删除
        R<Boolean> checkRes = carRentalPackageOrderService.checkByRentalPackageId(id);
        if (!checkRes.isSuccess()) {
            return R.fail(checkRes.getErrCode(), checkRes.getErrMsg());
        }
        if (checkRes.getData()) {
            return R.fail("300103", "已有购买订单记录，不允许删除");
        }

        // 操作 DB
        long delTime = System.currentTimeMillis();
        int num = carRentalPackageMapper.delById(id, uid, delTime);

        // 删除缓存
        delCache(String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_ID_KEY, id));

        return R.ok(num >= 0);
    }

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<List<CarRentalPackagePO>> list(CarRentalPackageQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
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
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
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
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageMapper.count(qryModel));
    }

    /**
     * 根据ID查询<br />
     * 可能返回<code>null</code>
     * @param id 主键ID
     * @return
     */
    @Slave
    @Override
    public CarRentalPackagePO selectById(Long id) {
        if (null == id || id <= 0) {
            return null;
        }

        // 获取缓存
        String cacheKey = String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_ID_KEY, id);
        CarRentalPackagePO cachePO = redisService.getWithHash(cacheKey, CarRentalPackagePO.class);
        if (ObjectUtils.isNotEmpty(cachePO)) {
            return null;
        }

        // 查询 DB
        CarRentalPackagePO dbPO = carRentalPackageMapper.selectById(id);

        // 存入缓存
        redisService.saveWithHash(cacheKey, dbPO);

        return dbPO;
    }

    /**
     * 根据ID修改数据
     * @param optModel 操作模型
     * @return
     */
    @Override
    public R<Boolean> updateById(CarRentalPackageOptModel optModel) {
        if (optModel == null || optModel.getId() == null || optModel.getId() <= 0 || optModel.getUpdateUid() == null || optModel.getUpdateUid() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 检测原始套餐状态
        CarRentalPackagePO oriEntity = carRentalPackageMapper.selectById(optModel.getId());
        if (oriEntity == null || DelFlagEnum.DEL.getCode().equals(oriEntity.getDelFlag())) {
            return R.fail("300101", "套餐不存在");
        }
        if (UpDownEnum.UP.getCode().equals(oriEntity.getStatus())) {
            return R.fail("300102", "上架状态的套餐不允许修改");
        }

        Integer tenantId = optModel.getTenantId();
        String name = optModel.getName();

        // 检测唯一
        if (!oriEntity.getName().equals(name) && carRentalPackageMapper.uqByTenantIdAndName(tenantId, name) > 0) {
            return R.fail("300100", "套餐名称已存在");
        }

        CarRentalPackagePO entity = new CarRentalPackagePO();
        BeanUtils.copyProperties(optModel, entity);

        // 赋值修改时间
        long now = System.currentTimeMillis();
        entity.setUpdateTime(now);

        int num = carRentalPackageMapper.updateById(entity);

        // 删除缓存
        String cacheEky = String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_ID_KEY, optModel.getId());
        redisService.delete(cacheEky);

        return R.ok(num >= 0);
    }

    /**
     * 新增数据，返回主键ID<br />
     * 若为车电一体，则会联动调用换电套餐的逻辑
     * @param optModel 操作模型
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Long> insert(CarRentalPackageOptModel optModel) {
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

        // 后续处理逻辑
        afterInsert(entity);

        return R.ok(entity.getId());
    }

    /**
     * 新增之后的后续操作
     * @param entity
     */
    private void afterInsert(CarRentalPackagePO entity) {
        // TODO 若为车电一体的套餐，需要调用换电套餐的接口，志龙
        if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(entity.getType())) {

        }
    }

    /**
     * 删除缓存
     * @param key
     */
    private void delCache(String key) {
        redisService.delete(key);
    }
}
