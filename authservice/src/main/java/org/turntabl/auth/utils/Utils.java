package org.turntabl.auth.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class Utils {
    public static LocalDateTime convertTimestampToDate(Timestamp time) {
        if (time == null)
            return null;
        LocalDateTime localDateTime = time.toLocalDateTime();
        return localDateTime;
    }
}
