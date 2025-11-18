package com.lanf.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.user.model.dto.CreateAddressDTO;
import com.lanf.user.model.dto.SetDefaultAddressDTO;
import com.lanf.user.model.entity.AddressDO;
import com.lanf.user.model.vo.AddressVO;

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

    void  createAddress(CreateAddressDTO dto);

    AddressDO getDefaultAddress();


    List<AddressVO> listAddress();

    void  setDefaultAddress(SetDefaultAddressDTO dto );


}
