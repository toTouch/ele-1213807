package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.Provincial;
import com.xiliulou.electricity.mapper.ProvincialMapper;
import com.xiliulou.electricity.service.CityService;
import com.xiliulou.electricity.service.ProvincialService;
import com.xiliulou.electricity.vo.ProvincialVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * (Provincial)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 16:20:43
 */
@Service("provincialService")
public class ProvincialServiceImpl implements ProvincialService {
    @Resource
    private ProvincialMapper provincialMapper;
    @Autowired
    private CityService cityService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param pid 主键
     * @return 实例对象
     */
    @Override
    public Provincial queryByIdFromDB(Integer pid) {
        return this.provincialMapper.queryById(pid);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param pid 主键
     * @return 实例对象
     */
    @Override
    public  Provincial queryByIdFromCache(Integer pid) {
        return null;
    }



    @Override
    public R test() {
        List<Provincial> provincialList=provincialMapper.selectList(Wrappers.<Provincial>lambdaQuery());
        List<ProvincialVO> provincialVOList=new ArrayList<>();
        if(ObjectUtil.isNotEmpty(provincialList)){
            for (Provincial provincial:provincialList) {
                ProvincialVO provincialVO=new ProvincialVO();
                BeanUtil.copyProperties(provincial,provincialVO);
                List<City> cityList=cityService.queryCityListByPid(provincialVO.getPid());
                provincialVO.setCityList(cityList);
                provincialVOList.add(provincialVO);
            }
        }
        return R.ok(provincialVOList.stream().sorted(Comparator.comparing(ProvincialVO::getPid)).collect(Collectors.toList()));

    }



}