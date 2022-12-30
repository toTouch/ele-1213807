package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.Province;
import com.xiliulou.electricity.entity.Region;
import com.xiliulou.electricity.mapper.CityMapper;
import com.xiliulou.electricity.mapper.ProvinceMapper;
import com.xiliulou.electricity.service.CityService;
import com.xiliulou.electricity.service.ProvinceService;
import com.xiliulou.electricity.service.RegionService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * (Province)表服务实现类
 *
 * @author makejava
 * @since 2021-01-21 18:05:46
 */
@Service("provinceService")
@Slf4j
public class ProvinceServiceImpl implements ProvinceService {
    @Resource
    private ProvinceMapper provinceMapper;

    @Autowired
    CityMapper cityMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Province queryByIdFromDB(Integer id) {
        return this.provinceMapper.selectById(id);
    }


    @Override
    public List<Province> queryList() {
        return this.provinceMapper.queryAllCity();
    }

    private static final String CITY_LIST_URL = "https://apis.map.qq.com/ws/district/v1/list?key=FL6BZ-5J7CO-UFEWY-SOMRG-KLTC6-UHFU7";

    private static final String CITY_CHILDREN_LIST_URL = "https://apis.map.qq.com/ws/district/v1/getchildren?key=FL6BZ-5J7CO-UFEWY-SOMRG-KLTC6-UHFU7&id=";


    @Autowired
    RegionService regionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void test() throws Exception {


        //根据省获取市
        String city = HttpUtil.get(CITY_CHILDREN_LIST_URL + "810000", CharsetUtil.CHARSET_UTF_8);

        CityResult reallyCityResult = JSON.parseObject(city, CityResult.class);
        List<List<CityDTO>> reallyCityList = reallyCityResult.getResult();
        if (CollectionUtils.isNotEmpty(reallyCityList)) {
            for (List<CityDTO> cityDTOS : reallyCityList) {
                if (CollectionUtils.isNotEmpty(cityDTOS)) {
                    for (CityDTO cityDTO : cityDTOS) {
                        Region region = new Region();
                        region.setCode(cityDTO.getId());
                        region.setName(cityDTO.getFullname());
                        region.setPid(460);
                        log.error(JsonUtil.toJson(region));
                        regionService.insert(region);
                    }
                }
            }
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //根据省获取市
        String city2 = HttpUtil.get(CITY_CHILDREN_LIST_URL + "820000", CharsetUtil.CHARSET_UTF_8);

        CityResult reallyCityResult2 = JSON.parseObject(city2, CityResult.class);
        List<List<CityDTO>> reallyCityList2 = reallyCityResult2.getResult();
        if (CollectionUtils.isNotEmpty(reallyCityList2)) {
            for (List<CityDTO> cityDTOS : reallyCityList2) {
                if (CollectionUtils.isNotEmpty(cityDTOS)) {
                    for (CityDTO cityDTO : cityDTOS) {
                        Region region2 = new Region();
                        region2.setCode(cityDTO.getId());
                        region2.setName(cityDTO.getFullname());
                        region2.setPid(461);
                        log.error(JsonUtil.toJson(region2));
                        regionService.insert(region2);
                    }
                }
            }
        }


//        List<City> cities = cityMapper.selectList(new LambdaQueryWrapper<City>().ne(City::getCode, 0));
//        cities.forEach(item -> {
//            try {
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            //根据省获取市
//            String city = HttpUtil.get(CITY_CHILDREN_LIST_URL + item.getCode(), CharsetUtil.CHARSET_UTF_8);
//
//            CityResult reallyCityResult = JSON.parseObject(city, CityResult.class);
//            List<List<CityDTO>> reallyCityList = reallyCityResult.getResult();
//            if (CollectionUtils.isNotEmpty(reallyCityList)) {
//                for (List<CityDTO> cityDTOS : reallyCityList) {
//                    if (CollectionUtils.isNotEmpty(cityDTOS)) {
//                        for (CityDTO cityDTO : cityDTOS) {
//                            City cityy = new City();
//                            cityy.setCode(cityDTO.getId());
//                            cityy.setName(cityDTO.getFullname());
//                            cityy.setPid(item.getId());
//                            log.error(JsonUtil.toJson(cityy));
//                            cityMapper.insert(cityy);
//                        }
//                    }
//
//
//                }
//            }
//        });

    }
}

@Data
class CityResult {

    private List<List<CityDTO>> result;
}

@Data
class CityDTO {
    private String id;
    private String fullname;
}
