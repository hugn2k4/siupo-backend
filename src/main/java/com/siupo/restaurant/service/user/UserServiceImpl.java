package com.siupo.restaurant.service.user;

import com.siupo.restaurant.dto.request.ChangePasswordRequest;
import com.siupo.restaurant.dto.request.UserRequest;
import com.siupo.restaurant.dto.response.UserResponse;
import com.siupo.restaurant.enums.EUserStatus;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.exception.business.ForbiddenException;
import com.siupo.restaurant.exception.business.NotFoundException;
import com.siupo.restaurant.exception.business.UnauthorizedException;
import com.siupo.restaurant.mapper.UserMapper;
import com.siupo.restaurant.model.Customer;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.repository.CustomerRepository;
import com.siupo.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.siupo.restaurant.model.Image;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentCustomerInfo(User user) {
        if (user == null)
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        if (!(user instanceof Customer)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }
        if (user.getAvatar() != null) {
            user.getAvatar().getId();
        }
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateCustomerInfo(User user, UserRequest request) {
        if (user == null) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        }
        updateBasicInfo(user, request);
        updateAvatar(user, request);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (user == null)
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword()))
            throw new BadRequestException(ErrorCode.INVALID_PASSWORD);
        if (!request.getNewPassword().equals(request.getConfirmNewPassword()))
            throw new BadRequestException(ErrorCode.PASSWORD_MISMATCH);
        if (request.getOldPassword().equals(request.getNewPassword()))
            throw new BadRequestException(ErrorCode.NEW_PASSWORD_SAME_AS_OLD);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public List<UserResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void updateCustomerStatus(Long customerId, EUserStatus status) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        customer.setStatus(status);
        customerRepository.save(customer);
    }

    private void updateBasicInfo(User user, UserRequest request) {
        if (StringUtils.hasText(request.getFullName()))
            user.setFullName(request.getFullName());
        if (StringUtils.hasText(request.getPhoneNumber()))
            user.setPhoneNumber(request.getPhoneNumber());
        if (request.getDateOfBirth() != null)
            user.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null)
            user.setGender(request.getGender());
    }

    private void updateAvatar(User user, UserRequest request) {
        Image avatar = user.getAvatar();
        String newUrl = request.getAvatarUrl();
        // Case 1: client muốn xoá avatar
        if (!StringUtils.hasText(newUrl)) {
            user.setAvatar(null);
            return;
        }
        // Case 2: chưa có avatar → tạo mới
        if (avatar == null) {
            avatar = Image.builder()
                    .url(newUrl)
                    .name(request.getAvatarName())
                    .build();
            user.setAvatar(avatar);
            return;
        }
        // Case 3: đã có avatar → update
        avatar.setUrl(newUrl);
        avatar.setName(request.getAvatarName());
    }
}
