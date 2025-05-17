package me.iivye.plugin.briar.schedule;

import me.iivye.plugin.briar.Briar;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class ScheduleUtils {
    public static LocalDateTime getDateNearest(List<LocalDateTime> dates, LocalDateTime targetDate) {
        final ZoneOffset offset = Briar.getInstance().getTimezone().getRules().getOffset(targetDate);
        long minDiff = -1, currentTime = targetDate.toEpochSecond(offset);
        LocalDateTime minDate = null;
        for (LocalDateTime date : dates) {
            long diff = Math.abs(currentTime - date.toEpochSecond(offset));
            if ((minDiff == -1) || (diff < minDiff)) {
                minDiff = diff;
                minDate = date;
            }
        }
        return minDate;
    }
}
