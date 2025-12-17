package com.siupo.restaurant.service.user;

import com.siupo.restaurant.dto.request.ChangePasswordRequest;
import com.siupo.restaurant.dto.request.UserRequest;
import com.siupo.restaurant.enums.EUserStatus;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.repository.UserRepository;
import jakarta.persistence.DiscriminatorValue;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import com.siupo.restaurant.model.Image;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUserInfo(User user) {
        if (user == null)
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new UnauthorizedException("User not authenticated");

        // Buộc Entity Avatar (LAZY) được tải trong Transaction
        if (user.getAvatar() != null) {
            // Chỉ cần truy cập một thuộc tính bất kỳ (ví dụ: getId()) để khởi tạo proxy.
            user.getAvatar().getId();
        }

        return user;
    }

    @Override
    @Transactional
    public User updateUserInfo(User user, UserRequest request) {
        if (user == null)
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new UnauthorizedException("User not authenticated");

        if (request.getFullName() != null && !request.getFullName().isBlank()) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getAvatarUrl() != null || user.getAvatar() != null) {
            Image existingAvatar = user.getAvatar();

            if (existingAvatar == null && request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
                // Tạo Image Entity mới nếu chưa có và có URL mới hợp lệ
                existingAvatar = Image.builder().url(request.getAvatarUrl()).name(request.getAvatarName()).build();
                user.setAvatar(existingAvatar);
            }

            if (existingAvatar != null) {
                if (request.getAvatarUrl() == null || request.getAvatarUrl().isBlank()) {
                    // Nếu URL gửi lên là NULL hoặc rỗng, xóa Avatar
                    user.setAvatar(null); // orphanRemoval=true sẽ xử lý xóa Image Entity
                } else {
                    // Cập nhật thông tin Avatar
                    existingAvatar.setUrl(request.getAvatarUrl());
                    existingAvatar.setName(request.getAvatarName());
                }
            }
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (user == null)
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new UnauthorizedException("User not authenticated");

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword()))
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new BadRequestException("Mật khẩu cũ không chính xác");
        if (!request.getNewPassword().equals(request.getConfirmNewPassword()))
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        if (request.getOldPassword().equals(request.getNewPassword()))
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new BadRequestException("Mật khẩu mới phải khác mật khẩu cũ");

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    @Override
    public List<User> getAllCustomers() {
        return userRepository.findAll().stream()
                .filter(user -> "CUSTOMER".equals(user.getClass().getAnnotation(DiscriminatorValue.class).value()))
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void updateCustomerStatus(Long customerId, EUserStatus status) {
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng với ID: " + customerId));
        if (!"CUSTOMER".equals(user.getClass().getAnnotation(DiscriminatorValue.class).value())) {
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new BadRequestException("Chỉ có thể cập nhật trạng thái của CUSTOMER");
        }

        user.setStatus(status);
        userRepository.save(user);
    }

}
