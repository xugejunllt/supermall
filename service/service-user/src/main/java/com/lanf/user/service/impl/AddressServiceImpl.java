package com.lanf.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.lock.aop.DistributedLock;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.security.utils.UserContext;
import com.lanf.security.utils.UserUtils;
import com.lanf.user.mapper.AddressMapper;
import com.lanf.user.model.dto.CreateAddressDTO;
import com.lanf.user.model.dto.SetDefaultAddressDTO;
import com.lanf.user.model.entity.AddressDO;
import com.lanf.user.model.vo.AddressVO;
import com.lanf.user.service.IAddressService;
import com.lanf.user.service.manager.AddersCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-10
 */
@Slf4j
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, AddressDO> implements IAddressService {

    @Autowired
    private AddersCache addersCache;

    @Override
    @Transactional //添加事务注解 如果redis操作失败 进行回滚
    @DistributedLock(key = "#dto.userId")
    public void createAddress(CreateAddressDTO dto) {
        AddressDO addressDO = new AddressDO();
        BeanCopyUtils.copy(dto, addressDO);
        Long userId = UserContext.getUserId();
        addressDO.setUserId(userId);
        List<AddressDO> list = this.lambdaQuery().eq(AddressDO::getUserId, userId).list();
        if (list.isEmpty()) {
            //只有一条，设置为默认地址
            addressDO.setDefaultAddress(0);
        } else {
            addressDO.setDefaultAddress(1);
        }
        this.save(addressDO);
        addersCache.removeCache(userId);
    }

    @Override
    public AddressDO getDefaultAddress() {

        Long userId = UserContext.getUserId();

        return this.lambdaQuery().eq(AddressDO::getUserId, userId).eq(AddressDO::getDefaultAddress, 0).one();
    }

    @Override
    public List<AddressVO> listAddress() {

        Long userId = UserContext.getUserId();

        List<AddressVO> addressVOList = addersCache.getCache(userId);
        if (addressVOList != null){

            return addressVOList;
        }

        List<AddressDO> list = this.lambdaQuery().eq(AddressDO::getUserId, userId).
                orderByDesc(BaseEntity::getUpdateTime).list();
        if (list.isEmpty()){

            return new ArrayList<>();
        }
        List<AddressVO> addressVOS = BeanCopyUtils.copyBeanList(list, AddressVO.class);
        try {
            addersCache.addCache(userId,addressVOS);
        } catch (Exception e) {
            //添加缓存失败 进行降级 不影响读接口
            log.warn("地址信息添加缓存失败");
        }
        return  addressVOS;
    }


    @Override
    @Transactional //添加事务注解 如果redis操作失败 进行回滚
    @DistributedLock(key = "#dto.userId")
    public void setDefaultAddress(SetDefaultAddressDTO dto) {

        //把上个默认地址更新
        AddressDO defaultAddress = getDefaultAddress();
        AddressDO updateAddressDO = new AddressDO();
        updateAddressDO.setId(defaultAddress.getId());
        updateAddressDO.setDefaultAddress(1);

        //更新当前地址为默认地址
        Long id = dto.getId();
        AddressDO updateAddressDO2 = new AddressDO();
        updateAddressDO2.setId(id);
        updateAddressDO2.setDefaultAddress(0);
        List<AddressDO> addressDOList = new ArrayList<>();
        addressDOList.add(updateAddressDO);
        addressDOList.add(updateAddressDO2);
        this.updateBatchById(addressDOList);
        addersCache.removeCache(UserContext.getUserId());
    }
}
