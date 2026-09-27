package br.com.fiap.fordradar.security;

import br.com.fiap.fordradar.models.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;


public final class AuditLog {

    private static final Logger LOG = LoggerFactory.getLogger("AUDIT");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AuditLog() {
    }

    public static void event(String type, String outcome, Object... keyValues) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("ts", Instant.now().toString());
        entry.put("event", type);
        entry.put("outcome", outcome);
        entry.put("ip", currentIp());
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            entry.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        try {
            LOG.info(MAPPER.writeValueAsString(entry));
        } catch (Exception e) {
            LOG.warn("audit serialization failed for event {}", type);
        }
    }

    public static String hash(String value) {
        if (value == null) {
            return "n/a";
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.toLowerCase().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 12);
        } catch (Exception e) {
            return "n/a";
        }
    }

    public static String maskVin(String vin) {
        if (vin == null || vin.length() < 4) {
            return "****";
        }
        return "*".repeat(vin.length() - 4) + vin.substring(vin.length() - 4);
    }

    public static String currentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return hash(user.getEmail());
        }
        return "anonymous";
    }

    private static String currentIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest().getRemoteAddr();
        }
        return "n/a";
    }
}
