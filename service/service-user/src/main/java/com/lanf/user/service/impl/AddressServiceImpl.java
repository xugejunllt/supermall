package com.lanf.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.security.utils.UserUtils;
import com.lanf.user.mapper.AddressMapper;
import com.lanf.user.model.dto.AddressAddDTO;
import com.lanf.user.model.dto.SetDefaultAddressDTO;
import com.lanf.user.model.entity.AddressDO;
import com.lanf.user.service.IAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-10
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, AddressDO> implements IAddressService {

    @Override
    public void addAddress(AddressAddDTO dto) {
        AddressDO addressDO = new AddressDO();
        BeanCopyUtils.copy(dto, addressDO);
        Long userId = UserUtils.getUserId();
        addressDO.setMemberId(userId);
        List<AddressDO> list = this.lambdaQuery().eq(AddressDO::getMemberId, userId).list();
        if (list.isEmpty()) {
            //只有一条，设置为默认地址
            addressDO.setDefaultAddress(0);
        } else {
            addressDO.setDefaultAddress(1);
        }
        this.save(addressDO);

    }

    @Override
    public AddressDO getDefaultAddress() {
        Long userId = UserUtils.getUserId();

        return this.lambdaQuery().eq(AddressDO::getMemberId, userId).eq(AddressDO::getDefaultAddress, 0).one();
    }

    @Override
    public List<AddressDO> addressList() {

        return this.lambdaQuery().eq(AddressDO::getMemberId, UserUtils.getUserId() ).orderByDesc(BaseEntity::getCreateTime).list();
    }

    @Transactional
    @Override
    public void setDefaultAddress(SetDefaultAddressDTO dto) {

        //把上个默认地址更新
        AddressDO defaultAddress = getDefaultAddress();
        AddressDO updateAddressDO = new AddressDO();
        updateAddressDO.setId(defaultAddress.getId());
        updateAddressDO.setDefaultAddress(1);
        this.updateById(updateAddressDO);

        //更新当前地址为默认地址
        Long id = dto.getId();
        AddressDO updateAddressDO2 = new AddressDO();
        updateAddressDO2.setId(id);
        updateAddressDO2.setDefaultAddress(0);
        this.updateById(updateAddressDO2);
    }
}
