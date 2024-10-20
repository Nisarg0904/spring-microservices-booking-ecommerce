package ca.gbc.userservice.service;

import ca.gbc.userservice.dto.UserRequest;
import ca.gbc.userservice.dto.UserResponse;
import ca.gbc.userservice.model.User;
import ca.gbc.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(user.getId(),user.getName(),user.getEmail(),user.getPassword(),user.getRole());
    }
    private User convertToUser(UserRequest userRequest) {
        return User.builder()
                .name(userRequest.name())
                .email(userRequest.email())
                .password(userRequest.password())
                .role(userRequest.role())
                .userType(userRequest.userType())
                .build();
    }


    @Override
    public UserResponse createUser(UserRequest userRequest) {
        User user = convertToUser(userRequest);
        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);    }

    @Override
    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id).map(this::convertToUserResponse);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());    }

    @Override
    public Optional<UserResponse> updateUser(Long id, UserRequest userRequest) {
        return userRepository.findById(id).map(user -> {
            user.setName(userRequest.name());
            user.setEmail(userRequest.email());
            user.setPassword(userRequest.password()); // Ensure this is hashed
            user.setRole(userRequest.role());
            user.setUserType(userRequest.userType());
            User updatedUser = userRepository.save(user);
            return convertToUserResponse(updatedUser);
        });
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<UserResponse> getUsersByRole(String role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());    }

    @Override
    public List<UserResponse> getUsersByUserType(String userType) {
        return userRepository.findByUserType(userType).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());    }
}
