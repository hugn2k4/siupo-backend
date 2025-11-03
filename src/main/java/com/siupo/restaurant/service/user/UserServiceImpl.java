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
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    public User getCurrentUserInfo(User user) {
        if (user == null) throw new UnauthorizedException("User not authenticated");
        return user;
    }

    @Override
    @Transactional
    public User updateUserInfo(User user, UserRequest request) {
        if (user == null) throw new UnauthorizedException("User not authenticated");

        if (request.getFullName() != null && !request.getFullName().isBlank()) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) user.setGender(request.getGender());

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (user == null) throw new UnauthorizedException("User not authenticated");

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword()))
            throw new BadRequestException("Mật khẩu cũ không chính xác");
        if (!request.getNewPassword().equals(request.getConfirmNewPassword()))
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        if (request.getOldPassword().equals(request.getNewPassword()))
            throw new BadRequestException("Mật khẩu mới phải khác mật khẩu cũ");

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
