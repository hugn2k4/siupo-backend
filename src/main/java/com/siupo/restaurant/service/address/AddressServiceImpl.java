package com.siupo.restaurant.service.address;

import com.siupo.restaurant.dto.AddressDTO;
import com.siupo.restaurant.dto.request.AddressUpdateRequest;
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
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public AddressDTO addAddress(User user, AddressDTO dto) {
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
        if (addressCount == 1) { // Chỉ có 1 địa chỉ (vừa thêm)
            customer.setDefaultAddress(saved);
            userRepository.save(customer);
        }

        return toDTO(saved);
    }

    @Override
    @Transactional
    public AddressDTO updateAddressByContent(User user, AddressDTO dto) {
        Address address = findAddressByContent(user, dto);
        address.setAddress(dto.getAddressLine());
        address.setWard(dto.getWard());
        address.setDistrict(dto.getDistrict());
        address.setProvince(dto.getProvince());
        address.setReceiverName(dto.getReceiverName());
        address.setReceiverPhone(dto.getReceiverPhone());
        return toDTO(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddressByContent(User user, AddressDTO dto) {
        Customer customer = getCustomer(user);
        Address address = findAddressByContent(user, dto);

        // Nếu là địa chỉ mặc định → không cho xóa
        if (customer.getDefaultAddress() != null &&
                customer.getDefaultAddress().equals(address)) { // DÙNG equals() HOẶC id
            throw new BadRequestException("Không thể xóa địa chỉ mặc định");
        }

        // CHỈ XÓA ADDRESS → KHÔNG SAVE CUSTOMER
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public AddressDTO setDefaultAddressByContent(User user, AddressDTO dto) {
        Customer customer = getCustomer(user);
        Address address = findAddressByContent(user, dto);
        customer.setDefaultAddress(address);
        userRepository.save(customer);
        return toDTO(address);
    }

    @Override
    public AddressDTO getDefaultAddress(User user) {
        Customer customer = getCustomer(user);
        Address def = customer.getDefaultAddress();
        return def != null ? toDTO(def) : null;
    }

    @Override
    public List<AddressDTO> getAddresses(User user) {
        return addressRepository.findByUser(user).stream()
                .map(this::toDTO)
                .toList();
    }

    private Customer getCustomer(User user) {
        if (!(user instanceof Customer customer)) {
            throw new UnauthorizedException("Chỉ customer mới có địa chỉ");
        }
        return customer;
    }

    private Address findAddressByContent(User user, AddressDTO dto) {
        return addressRepository.findByUserAndAllFields(
                        user,
                        dto.getAddressLine(),
                        dto.getWard(),
                        dto.getDistrict(),
                        dto.getProvince(),
                        dto.getReceiverName(),
                        dto.getReceiverPhone()
                )
                .orElseThrow(() -> new NotFoundException("Địa chỉ không tồn tại"));
    }

    private boolean isSameAddress(Address a1, Address a2) {
        return a1.getAddress().equals(a2.getAddress()) &&
                a1.getWard().equals(a2.getWard()) &&
                a1.getDistrict().equals(a2.getDistrict()) &&
                a1.getProvince().equals(a2.getProvince()) &&
                a1.getReceiverName().equals(a2.getReceiverName()) &&
                a1.getReceiverPhone().equals(a2.getReceiverPhone());
    }

    private AddressDTO toDTO(Address a) {
        return AddressDTO.builder()
                //.id(a.getId())
                .addressLine(a.getAddress())
                .ward(a.getWard())
                .district(a.getDistrict())
                .province(a.getProvince())
                .receiverName(a.getReceiverName())
                .receiverPhone(a.getReceiverPhone())
                .build();
    }
    @Transactional
    public AddressDTO updateAddressByOldAndNew(User user, AddressUpdateRequest request) {
        Address oldAddress = findAddressByContent(user, request.getOldAddress());

        // Cập nhật từng field từ newAddress (chỉ cập nhật field không null)
        updateIfNotNull(oldAddress::setAddress, request.getNewAddress().getAddressLine());
        updateIfNotNull(oldAddress::setWard, request.getNewAddress().getWard());
        updateIfNotNull(oldAddress::setDistrict, request.getNewAddress().getDistrict());
        updateIfNotNull(oldAddress::setProvince, request.getNewAddress().getProvince());
        updateIfNotNull(oldAddress::setReceiverName, request.getNewAddress().getReceiverName());
        updateIfNotNull(oldAddress::setReceiverPhone, request.getNewAddress().getReceiverPhone());

        return toDTO(addressRepository.save(oldAddress));
    }

    private <T> void updateIfNotNull(Consumer<T> setter, T value) {
        if (value != null && !value.toString().isBlank()) {
            setter.accept(value);
        }
    }

}
