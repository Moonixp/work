package org.turntabl.chatapp.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.turntabl.chatapp.security.SecurityUtils;

@Component
public class ChatAppUtils {

    public static LocalDateTime convertTimestampToDate(Timestamp time) {
        if (time == null)
            return null;
        LocalDateTime localDateTime = time.toLocalDateTime();
        return localDateTime;
    }

    public static Optional<ResponseEntity<?>> ManagersOnlyRouteCheck(SecurityUtils securityUtils) {
        if (!securityUtils.hasRole("MANAGER")) {
            return Optional.of(
                    ResponseEntity.status(403).body(Map.of("error", "forbidden: only managers can access this route")));
        }
        return Optional.empty();
    }
}
