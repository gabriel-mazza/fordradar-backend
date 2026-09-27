package br.com.fiap.fordradar.security;

import br.com.fiap.fordradar.models.User;
import br.com.fiap.fordradar.models.enums.Role;
import br.com.fiap.fordradar.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.admin-bootstrap.email:}")
    private String email;

    @Value("${security.admin-bootstrap.password:}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return;
        }
        if (password.length() < 12) {
            throw new IllegalStateException("ADMIN_PASSWORD deve ter no mínimo 12 caracteres.");
        }
        if (repository.findByEmail(email).isPresent()) {
            return;
        }
        repository.save(User.builder()
                .name("Administrador")
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.ADMIN)
                .build());
        AuditLog.event("ADMIN_BOOTSTRAP", "SUCCESS", "user", AuditLog.hash(email));
    }
}
