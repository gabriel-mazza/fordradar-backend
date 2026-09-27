package br.com.fiap.fordradar.services;

import br.com.fiap.fordradar.exceptions.ResourceNotFoundException;
import br.com.fiap.fordradar.models.User;
import br.com.fiap.fordradar.models.enums.Role;
import br.com.fiap.fordradar.repositories.UserRepository;
import br.com.fiap.fordradar.security.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    @Transactional
    public void changeRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Role previous = user.getRole();
        user.setRole(newRole);
        userRepository.save(user);

        AuditLog.event("ROLE_CHANGED", "SUCCESS",
                "actor", AuditLog.currentActor(),
                "target", AuditLog.hash(user.getEmail()),
                "from", previous.name(),
                "to", newRole.name());
    }
}
