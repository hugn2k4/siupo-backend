package com.siupo.restaurant.service.address;

import com.siupo.restaurant.dto.request.AddressRequest;
import com.siupo.restaurant.dto.response.AddressResponse;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.exception.business.NotFoundException;
import com.siupo.restaurant.exception.business.UnauthorizedException;
import com.siupo.restaurant.mapper.AddressMapper;
import com.siupo.restaurant.model.Address;
import com.siupo.restaurant.model.Customer;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.repository.AddressRepository;
import com.siupo.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    public List<AddressResponse> getAddresses(User user) {
        Customer customer = getCustomer(user);
        Address defaultAddress = customer.getDefaultAddress();
        return customer.getAddresses().stream()
                .map(address -> {
                    if (defaultAddress != null &&
                            defaultAddress.getId().equals(address.getId())) {
                        return addressMapper.toDefaultAddressResponse(address);
                    }
                    return addressMapper.toAddressResponse(address);
                })
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse addAddress(User user, AddressRequest address) {
        Customer customer = getCustomer(user);
        Address newAddress = Address.builder()
                .customer(customer)
                .address(address.getAddress())
                .ward(address.getWard())
                .district(address.getDistrict())
                .province(address.getProvince())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .build();
        Address saved = addressRepository.save(newAddress);
        if (customer.getDefaultAddress() == null) {
            customer.setDefaultAddress(saved);
            userRepository.save(customer);
        }
        return addressMapper.toAddressResponse(saved);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(User user, Long addressId, AddressRequest updateAddress) {
        Customer customer = getCustomer(user);
        Address address = addressRepository
                .findByIdAndCustomer(addressId, customer)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ADDRESS_NOT_FOUND));
        address.setAddress(updateAddress.getAddress());
        address.setWard(updateAddress.getWard());
        address.setDistrict(updateAddress.getDistrict());
        address.setProvince(updateAddress.getProvince());
        address.setReceiverName(updateAddress.getReceiverName());
        address.setReceiverPhone(updateAddress.getReceiverPhone());
        Address updated = addressRepository.save(address);
        return addressMapper.toAddressResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAddress(User user, Long addressId) {
        Customer customer = getCustomer(user);
        Address address = addressRepository
                .findByIdAndCustomer(addressId, customer)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ADDRESS_NOT_FOUND));
        if (customer.getDefaultAddress() != null &&
                customer.getDefaultAddress().getId().equals(address.getId())) {
            throw new BadRequestException(ErrorCode.CANNOT_DELETE_DEFAULT_ADDRESS);
        }
        addressRepository.delete(address);
    }

    @Override
    public AddressResponse getAddressDefault(User user) {
        Customer customer = getCustomer(user);
        Address defaultAddress = customer.getDefaultAddress();
        if(defaultAddress == null) {
            throw new NotFoundException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        return addressMapper.toDefaultAddressResponse(defaultAddress);
    }

    @Override
    @Transactional
    public AddressResponse setAddressDefault(User user, Long addressId) {
        Customer customer = getCustomer(user);
        Address address = addressRepository
                .findByIdAndCustomer(addressId, customer)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.ADDRESS_NOT_FOUND));
        customer.setDefaultAddress(address);
        userRepository.save(customer);
        return addressMapper.toDefaultAddressResponse(address);
    }

    private Customer getCustomer(User user) {
        if (user instanceof Customer customer) {
            return customer;
        }
        throw new BadRequestException(ErrorCode.NOT_CUSTOMER);
    }
}
