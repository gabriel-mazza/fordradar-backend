package br.com.fiap.fordradar.services;

import br.com.fiap.fordradar.dtos.LoginRequestDTO;
import br.com.fiap.fordradar.dtos.LoginResponseDTO;
import br.com.fiap.fordradar.dtos.RegisterRequestDTO;
import br.com.fiap.fordradar.exceptions.BusinessRuleException;
import br.com.fiap.fordradar.models.User;
import br.com.fiap.fordradar.models.enums.Role;
import br.com.fiap.fordradar.repositories.UserRepository;
import br.com.fiap.fordradar.security.AuditLog;
import br.com.fiap.fordradar.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
        try {
            UsernamePasswordAuthenticationToken usernamePassword =
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
            Authentication auth = this.authenticationManager.authenticate(usernamePassword);

            User user = (User) auth.getPrincipal();
            String token = tokenService.generateToken(user);
            AuditLog.event("LOGIN", "SUCCESS", "user", AuditLog.hash(user.getEmail()), "role", user.getRole().name());
            return new LoginResponseDTO(token);
        } catch (AuthenticationException ex) {
            AuditLog.event("LOGIN", "FAILURE", "user", AuditLog.hash(request.getEmail()),
                    "reason", ex.getClass().getSimpleName());
            throw ex;
        }
    }

    public void register(RegisterRequestDTO request) {
        if (this.repository.findByEmail(request.getEmail()).isPresent()) {
            AuditLog.event("REGISTER", "FAILURE", "user", AuditLog.hash(request.getEmail()), "reason", "duplicate");
            throw new BusinessRuleException("Não foi possível concluir o cadastro com os dados informados.");
        }

        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encryptedPassword)
                .role(Role.ANALISTA)
                .build();

        this.repository.save(newUser);
        AuditLog.event("REGISTER", "SUCCESS", "user", AuditLog.hash(request.getEmail()), "role", Role.ANALISTA.name());
    }
}
