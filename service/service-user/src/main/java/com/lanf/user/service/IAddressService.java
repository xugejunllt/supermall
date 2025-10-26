package com.lanf.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.user.model.dto.AddressAddDTO;
import com.lanf.user.model.dto.SetDefaultAddressDTO;
import com.lanf.user.model.entity.AddressDO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-10
 */
public interface IAddressService extends IService<AddressDO> {

    void  addAddress(AddressAddDTO dto);

    AddressDO getDefaultAddress();


    List<AddressDO> addressList();

    void  setDefaultAddress(SetDefaultAddressDTO dto );


}
