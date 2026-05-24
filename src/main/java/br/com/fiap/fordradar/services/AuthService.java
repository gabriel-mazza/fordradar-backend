package br.com.fiap.fordradar.services;

import br.com.fiap.fordradar.dtos.LoginRequestDTO;
import br.com.fiap.fordradar.dtos.LoginResponseDTO;
import br.com.fiap.fordradar.dtos.RegisterRequestDTO;
import br.com.fiap.fordradar.exceptions.BusinessRuleException;
import br.com.fiap.fordradar.models.User;
import br.com.fiap.fordradar.repositories.UserRepository;
import br.com.fiap.fordradar.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(LoginRequestDTO request) {
        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication auth = this.authenticationManager.authenticate(usernamePassword);

        String token = tokenService.generateToken((User) auth.getPrincipal());

        return new LoginResponseDTO(token);
    }

    public void register(RegisterRequestDTO request) {
        if (this.repository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessRuleException("User with this email already exists.");
        }

        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encryptedPassword)
                .role(request.getRole())
                .build();

        this.repository.save(newUser);
    }
}

