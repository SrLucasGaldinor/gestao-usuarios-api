package com.empresa.gestao_usuarios.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.empresa.gestao_usuarios.dto.RegisterRequest;
import com.empresa.gestao_usuarios.dto.UpdateRequest;
import com.empresa.gestao_usuarios.dto.UserResponse;
import com.empresa.gestao_usuarios.model.User;
import com.empresa.gestao_usuarios.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário cadastrado com este email");
        }

        User user = User.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role("USER")
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    public List<UserResponse> findAll() {
        return userRepository.findByAtivoTrue().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        return UserResponse.fromEntity(user);
    }

    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        return UserResponse.fromEntity(user);
    }
    
    public UserResponse update(Long id, UpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        user.setNome(request.getNome());
        return UserResponse.fromEntity(userRepository.save(user));
    }

    public void deleteById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        user.setAtivo(false);
        userRepository.save(user);
    }
}
