package com.siupo.restaurant.service.user;

import com.siupo.restaurant.dto.AddressDTO;
import com.siupo.restaurant.dto.request.ChangePasswordRequest;
import com.siupo.restaurant.dto.request.UserRequest;
import com.siupo.restaurant.exception.BadRequestException;
import com.siupo.restaurant.exception.NotFoundException;
import com.siupo.restaurant.exception.UnauthorizedException;
import com.siupo.restaurant.model.Address;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.repository.AddressRepository;
import com.siupo.restaurant.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;
    public UserServiceImpl(UserRepository userRepository, AddressRepository addressRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    public User getCurrentUserInfo(User user) {
        if (user == null) {
            throw new RuntimeException("User not authenticated");
        }
        return user;
    }

    @Override
    public User updateUserInfo(User user, UserRequest request) {
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        // Cập nhật các trường từ request
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        // Lưu vào cơ sở dữ liệu
        return userRepository.save(user);
    }

    @Override
    public List<AddressDTO> getUserAddresses(User user) {
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return addressRepository.findByUser(user).stream()
                .map(this::mapToAddressDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressDTO addAddress(User user, AddressDTO addressDTO) {
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        if (Boolean.TRUE.equals(addressDTO.getIsDefault())) {
            resetDefaultAddress(user);
        }
        Address address = Address.builder()
                .user(user)
                .address(addressDTO.getAddressLine())
                .ward(addressDTO.getWard())
                .district(addressDTO.getDistrict())
                .province(addressDTO.getProvince())
                .receiverName(addressDTO.getReceiverName())
                .receiverPhone(addressDTO.getReceiverPhone())
                .isDefault(Boolean.TRUE.equals(addressDTO.getIsDefault()))
                .build();

        Address saved = addressRepository.save(address);
        return mapToAddressDTO(saved);
    }

    @Override
    @Transactional
    public AddressDTO updateAddress(User user, Long addressId, AddressDTO addressDTO) {
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Địa chỉ không tồn tại"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Không có quyền sửa địa chỉ này");
        }

        if (Boolean.TRUE.equals(addressDTO.getIsDefault()) && !address.getIsDefault()) {
            resetDefaultAddress(user);
        }
        address.setAddress(addressDTO.getAddressLine());
        address.setWard(addressDTO.getWard());
        address.setDistrict(addressDTO.getDistrict());
        address.setProvince(addressDTO.getProvince());
        address.setReceiverName(addressDTO.getReceiverName());
        address.setReceiverPhone(addressDTO.getReceiverPhone());
        address.setIsDefault(Boolean.TRUE.equals(addressDTO.getIsDefault()));

        Address updated = addressRepository.save(address);
        return mapToAddressDTO(updated);
    }

    @Override
    @Transactional
    public void deleteAddress(User user, Long addressId) {
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Địa chỉ không tồn tại"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Không có quyền xóa địa chỉ này");
        }

        addressRepository.delete(address);
    }


    private AddressDTO mapToAddressDTO(Address address) {
        return AddressDTO.builder()
                .addressLine(address.getAddress())
                .ward(address.getWard())
                .district(address.getDistrict())
                .province(address.getProvince())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .isDefault(address.getIsDefault())
                .build();
    }

    private void resetDefaultAddress(User user) {
        addressRepository.findByUserAndIsDefaultTrue(user)
                .ifPresent(oldDefault -> {
                    oldDefault.setIsDefault(false);
                    addressRepository.save(oldDefault);
                });
    }

    @Override
    public void changePassword(User user, ChangePasswordRequest request) {

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu cũ không chính xác");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("Mật khẩu mới và xác nhận không khớp");
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("Mật khẩu mới phải khác mật khẩu cũ");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
