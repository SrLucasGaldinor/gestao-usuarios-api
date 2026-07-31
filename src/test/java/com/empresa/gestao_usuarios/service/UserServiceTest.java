package com.empresa.gestao_usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.empresa.gestao_usuarios.dto.RegisterRequest;
import com.empresa.gestao_usuarios.dto.UserResponse;
import com.empresa.gestao_usuarios.model.User;
import com.empresa.gestao_usuarios.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void register_DeveLancarConflito_QuandoEmailJaExiste() {
        RegisterRequest request = RegisterRequest.builder()
                .nome("Lucas Galdino")
                .email("lucas@example.com")
                .senha("senha123")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.CONFLICT);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_DeveCriptografarSenhaEsSalvar_QuandoEmailNaoExiste() {
        RegisterRequest request = RegisterRequest.builder()
                .nome("Lucas Galdino")
                .email("lucas@example.com")
                .senha("senhaPura123")
                .build();

        String senhaCriptografada = "$2a$10$senhaCriptografadaFake";

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getSenha())).thenReturn(senhaCriptografada);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User usuarioSalvo = userCaptor.getValue();
        assertThat(usuarioSalvo.getSenha()).isEqualTo(senhaCriptografada);
        assertThat(usuarioSalvo.getSenha()).isNotEqualTo(request.getSenha());
        assertThat(usuarioSalvo.getAtivo()).isTrue();
        assertThat(response).isNotNull();
    }

    @Test
    void deleteById_DeveMarcarComoInativo_EmVezDeRemover() {
        Long id = 1L;
        User usuarioExistente = User.builder()
                .id(id)
                .nome("Lucas Galdino")
                .email("lucas@example.com")
                .senha("senhaCriptografada")
                .ativo(true)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(usuarioExistente));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.deleteById(id);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getAtivo()).isFalse();
        verify(userRepository, never()).deleteById(any(Long.class));
    }

    @Test
    void deleteById_DeveLancarNotFound_QuandoUsuarioNaoExiste() {
        Long id = 999L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteById(id))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
    }

    @Test
    void findAll_DeveRetornarApenasUsuariosAtivos() {
        User usuario1 = User.builder()
                .id(1L)
                .nome("Lucas Galdino")
                .email("lucas@example.com")
                .senha("senha1")
                .ativo(true)
                .build();

        User usuario2 = User.builder()
                .id(2L)
                .nome("Maria Silva")
                .email("maria@example.com")
                .senha("senha2")
                .ativo(true)
                .build();

        when(userRepository.findByAtivoTrue()).thenReturn(List.of(usuario1, usuario2));

        List<UserResponse> resultado = userService.findAll();

        assertThat(resultado).hasSize(2);
        verify(userRepository, times(1)).findByAtivoTrue();
        verify(userRepository, never()).findAll();
    }
}
