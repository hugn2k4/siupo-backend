package com.siupo.restaurant.service.address;

import com.siupo.restaurant.dto.AddressDTO;
import com.siupo.restaurant.dto.request.AddressUpdateRequest;
import com.siupo.restaurant.dto.response.AddressResponse;
import com.siupo.restaurant.dto.response.MessageDataReponse;
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
import java.util.Optional;
import java.util.function.Consumer;

import static com.siupo.restaurant.dto.response.AddressResponse.mapAddressEntityToResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public List<AddressResponse> getAddresses(User user) {
        return addressRepository.findByUser(user).stream()
                .map(AddressResponse::mapAddressEntityToResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse addAddress(User user, AddressDTO dto) {
        Customer customer = getCustomer(user);

        Address address = Address.builder()
                .user(user)
                .address(dto.getAddress())
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

        return mapAddressEntityToResponse(saved);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressDTO updateAddress) {
        Optional<Address> address = addressRepository.findById(addressId);
        if (address.isEmpty()) {
            return mapAddressEntityToResponse(address.get());
        }
        Address addr = address.get();
        addr.setAddress(updateAddress.getAddress());
        addr.setWard(updateAddress.getWard());
        addr.setDistrict(updateAddress.getDistrict());
        addr.setProvince(updateAddress.getProvince());
        addr.setReceiverName(updateAddress.getReceiverName());
        addr.setReceiverPhone(updateAddress.getReceiverPhone());
        addressRepository.save(addr);

        return mapAddressEntityToResponse(addr);
    }

    @Override
    @Transactional
    public MessageDataReponse deleteAddress(User user, Long addressId) {
        Customer customer = getCustomer(user);
        Optional<Address> address = addressRepository.findById(addressId);
        if (address.isEmpty()) {
            return new MessageDataReponse(false, "400", "Địa chỉ không tồn tại");
        }
        Address addr = address.get();

        if (customer.getDefaultAddress() != null &&
                customer.getDefaultAddress().equals(addr)) {
            return new MessageDataReponse(false, "401", "Không thể xóa địa chỉ mặc định");
        }
        addressRepository.delete(addr);
        return new MessageDataReponse(true, "200", "Xóa địa chỉ thành công");
    }

    @Override
    public AddressResponse getAddressDefault(User user) {
        Customer customer = getCustomer(user);
        Address defaultAddress = customer.getDefaultAddress();
        if(defaultAddress == null) {
            return null;
        }
        return AddressResponse.builder()
                .id(defaultAddress.getId())
                .address(defaultAddress.getAddress())
                .ward(defaultAddress.getWard())
                .district(defaultAddress.getDistrict())
                .province(defaultAddress.getProvince())
                .receiverName(defaultAddress.getReceiverName())
                .receiverPhone(defaultAddress.getReceiverPhone())
                .isDefault(true)
                .build();
    }

    @Override
    @Transactional
    public AddressResponse setAddressDefault(User user, Long addressId) {
        Customer customer = getCustomer(user);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Địa chỉ không tồn tại"));
        if (!address.getUser().getId().equals(customer.getId())) {
            throw new UnauthorizedException("Địa chỉ không thuộc về người dùng");
        }
        customer.setDefaultAddress(address);
        userRepository.save(customer);

        return mapAddressEntityToResponse(address);
    }

    private Customer getCustomer(User user) {
        if (!(user instanceof Customer customer)) {
            throw new UnauthorizedException("Chỉ customer mới có địa chỉ");
        }
        return customer;
    }
}
