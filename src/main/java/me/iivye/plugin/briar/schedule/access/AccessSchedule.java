package me.iivye.plugin.briar.schedule.access;

import me.iivye.plugin.briar.Briar;
import me.iivye.plugin.briar.schedule.ScheduleType;

import java.time.LocalDateTime;
import java.util.Map;

public class AccessSchedule {
    private final LocalDateTime start, end;

    public AccessSchedule(ScheduleType type, Map<?, ?> data) {
        this.start = type.parse((String) data.get("start"));
        this.end = type.parse((String) data.get("end"));
    }

    public boolean isNowBetween() {
        final LocalDateTime now = LocalDateTime.now(Briar.getInstance().getTimezone());

        return now.isAfter(start) && now.isBefore(end);
    }

    public LocalDateTime getStart() {
        return start;
    }
}
