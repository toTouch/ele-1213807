package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CarRenalCacheConstant;
import com.xiliulou.electricity.constant.StringConstant;
import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.car.CarRentalPackageMapper;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
     * 根据主键ID查询，不区分是否删除
     *
     * @param ids 主键ID集
     * @return
     */
    @Slave
    @Override
    public List<CarRentalPackagePO> selectByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }

        return carRentalPackageMapper.selectByIds(ids);
    }

    /**
     * 根据条件查询<br />
     *
     * @param qryModel 查询条件
     * @return
     */
    @Slave
    @Override
    public List<CarRentalPackagePO> listByCondition(CarRentalPackageQryModel qryModel) {
        if (!ObjectUtils.allNotNull(qryModel, qryModel.getTenantId())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageMapper.list(qryModel);
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
        Boolean checkFlag = carRentalPackageOrderService.checkByRentalPackageId(id);
        if (checkFlag) {
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
    public List<CarRentalPackagePO> list(CarRentalPackageQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            throw  new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        handleQryModel(qryModel);

        return carRentalPackageMapper.list(qryModel);
    }

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public List<CarRentalPackagePO> page(CarRentalPackageQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            throw  new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        handleQryModel(qryModel);

        return carRentalPackageMapper.page(qryModel);
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

        handleQryModel(qryModel);

        return R.ok(carRentalPackageMapper.count(qryModel));
    }

    /**
     * 处理请求参数
     * @param qryModel
     */
    private void handleQryModel(CarRentalPackageQryModel qryModel) {
        // 处理电池型号，保底
        String batteryModelIds = qryModel.getBatteryModelIds();
        String batteryModelIdsLeftLike = qryModel.getBatteryModelIdsLeftLike();
        if (StringUtils.isNotBlank(batteryModelIds)) {
            batteryModelIds = Arrays.asList(batteryModelIds.split(StringConstant.COMMA_EN)).stream().sorted().collect(Collectors.joining(StringConstant.COMMA_EN));
            qryModel.setBatteryModelIds(batteryModelIds);
        }
        if (StringUtils.isNotBlank(batteryModelIdsLeftLike)) {
            batteryModelIdsLeftLike = Arrays.asList(batteryModelIdsLeftLike.split(StringConstant.COMMA_EN)).stream().sorted().collect(Collectors.joining(StringConstant.COMMA_EN));
            qryModel.setBatteryModelIdsLeftLike(batteryModelIdsLeftLike);
        }
    }

    /**
     * 根据ID查询<br />
     * 优先查询缓存，缓存没有查询DB，懒加载缓存<br />
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
            return R.fail("300102", "请先下架套餐再进行编辑操作");
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

        // 保底处理电池型号ID连接字符串
        String batteryModelIds = entity.getBatteryModelIds();
        if (StringUtils.isNotBlank(batteryModelIds)) {
            batteryModelIds = Arrays.asList(batteryModelIds.split(StringConstant.COMMA_EN)).stream().sorted().collect(Collectors.joining(StringConstant.COMMA_EN));
            entity.setBatteryModelIds(batteryModelIds);
        }


        // 赋值操作人、时间、删除标记
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setDelFlag(DelFlagEnum.OK.getCode());

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
