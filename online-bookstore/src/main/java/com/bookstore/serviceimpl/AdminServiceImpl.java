package com.bookstore.serviceimpl;

import com.bookstore.bean.AppProperties;
import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapping.UserMapper;
import com.bookstore.object.response.PageResponse;
import com.bookstore.object.response.UserResponse;
import com.bookstore.repository.UserRepository;
import com.bookstore.service.AdminService;
import com.bookstore.utility.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AppProperties appProperties;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(int page, int size) {
        return PageResponse.from(
                userRepository.findAll(PaginationUtils.of(page, size, appProperties.maxPageSize(), Sort.by("id"))),
                userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return userMapper.toResponse(findUser(id));
    }

    @Override
    public UserResponse changeRole(String currentAdmin, Long userId, Role role) {
        User user = findUser(userId);
        if (user.getUsername().equalsIgnoreCase(currentAdmin) && role != Role.ADMIN) {
            // Prevents the last administrator from locking everyone out by accident
            throw new BadRequestException("You cannot remove your own Administrator role");
        }
        user.setRole(role);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse changeStatus(String currentAdmin, Long userId, boolean enabled) {
        User user = findUser(userId);
        if (user.getUsername().equalsIgnoreCase(currentAdmin) && !enabled) {
            throw new BadRequestException("You cannot disable your own account");
        }
        user.setEnabled(enabled);
        return userMapper.toResponse(userRepository.save(user));
    }

    private User findUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
