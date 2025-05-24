package me.iivye.plugin.briarmarket.schedule.access;

import me.iivye.plugin.briarmarket.Briar;
import me.iivye.plugin.briarmarket.schedule.ScheduleType;
import org.bukkit.configuration.ConfigurationSection;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static me.iivye.plugin.briarmarket.schedule.ScheduleUtils.getDateNearest;

public class AccessScheduleManager {
    private final Set<AccessSchedule> schedules = new HashSet<>();

    public AccessScheduleManager(Briar plugin) {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("access_schedule");
        ScheduleType mode = ScheduleType.fromName(config.getString("mode"));
        List<Map<?, ?>> scheduleMap;

        switch (mode) {
            case DATE -> scheduleMap = config.getMapList("dates");
            case TIMES -> scheduleMap = config.getMapList("times");
            default -> throw new UnsupportedOperationException("Unsupported schedule mode: " + mode);
        }

        for (Map<?, ?> map : scheduleMap) {
            schedules.add(new AccessSchedule(mode, map));
        }
    }

    public boolean isShopOpen() {
        return schedules.stream().anyMatch(AccessSchedule::isNowBetween);
    }

    public LocalDateTime getNextTime() {
        LocalDateTime now = LocalDateTime.now(Briar.getInstance().getTimezone());

        if (isShopOpen()) {
            return now;
        }

        List<LocalDateTime> startTimes = schedules.stream()
                .map(AccessSchedule::getStart)
                .collect(Collectors.toList());

        System.out.println(startTimes);

        return getDateNearest(startTimes, now);
    }
}

