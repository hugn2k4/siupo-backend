package com.siupo.restaurant.service.address;

import com.siupo.restaurant.dto.AddressDTO;
import com.siupo.restaurant.dto.response.AddressResponse;
import com.siupo.restaurant.exception.BadRequestException;
import com.siupo.restaurant.exception.NotFoundException;
import com.siupo.restaurant.exception.UnauthorizedException;
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

    @Override
    @Transactional
    public AddressResponse addAddress(User user, AddressDTO dto) {
        Customer customer = getCustomer(user);

        Address address = Address.builder()
                .user(user)
                .address(dto.getAddressLine())
                .ward(dto.getWard())
                .district(dto.getDistrict())
                .province(dto.getProvince())
                .receiverName(dto.getReceiverName())
                .receiverPhone(dto.getReceiverPhone())
                .build();

        Address saved = addressRepository.save(address);

        long addressCount = addressRepository.countByUserId(customer.getId());
        if (addressCount == 1) {
            customer.setDefaultAddress(saved);
            userRepository.save(customer);
        }

        return toResponse(saved, customer);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(User user, Long id, AddressDTO dto) {
        Customer customer = getCustomer(user);
        Address address = getAddressByIdAndUser(id, user);

        address.setAddress(dto.getAddressLine());
        address.setWard(dto.getWard());
        address.setDistrict(dto.getDistrict());
        address.setProvince(dto.getProvince());
        address.setReceiverName(dto.getReceiverName());
        address.setReceiverPhone(dto.getReceiverPhone());

        return toResponse(addressRepository.save(address), customer);
    }

    @Override
    @Transactional
    public void deleteAddress(User user, Long id) {
        Customer customer = getCustomer(user);
        Address address = getAddressByIdAndUser(id, user);

        if (customer.getDefaultAddress() != null && customer.getDefaultAddress().getId().equals(id)) {
            throw new BadRequestException("Không thể xóa địa chỉ mặc định");
        }

        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(User user, Long id) {
        Customer customer = getCustomer(user);
        Address address = getAddressByIdAndUser(id, user);
        customer.setDefaultAddress(address);
        userRepository.save(customer);
        return toResponse(address, customer);
    }

    @Override
    public AddressResponse getDefaultAddress(User user) {
        Customer customer = getCustomer(user);
        Address def = customer.getDefaultAddress();
        return def != null ? toResponse(def, customer) : null;
    }

    @Override
    public List<AddressResponse> getAddresses(User user) {
        Customer customer = getCustomer(user);
        return addressRepository.findByUser(user).stream()
                .map(address -> toResponse(address, customer))
                .toList();
    }

    private Customer getCustomer(User user) {
        if (!(user instanceof Customer customer)) {
            throw new UnauthorizedException("Chỉ customer mới có địa chỉ");
        }
        return customer;
    }

    private Address getAddressByIdAndUser(Long id, User user) {
        return addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Địa chỉ không tồn tại hoặc không có quyền"));
    }

    private AddressResponse toResponse(Address a, Customer customer) {
        boolean isDefault = customer.getDefaultAddress() != null
                && customer.getDefaultAddress().getId().equals(a.getId());

        return AddressResponse.builder()
                .id(a.getId())
                .addressLine(a.getAddress())
                .ward(a.getWard())
                .district(a.getDistrict())
                .province(a.getProvince())
                .receiverName(a.getReceiverName())
                .receiverPhone(a.getReceiverPhone())
                .isDefault(isDefault)
                .build();
    }
}
