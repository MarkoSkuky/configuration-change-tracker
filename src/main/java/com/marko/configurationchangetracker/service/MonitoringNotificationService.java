package com.marko.configurationchangetracker.service;

import com.marko.configurationchangetracker.model.ConfigurationChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MonitoringNotificationService {

    public void notifyCriticalChange(ConfigurationChange change) {
        log.warn(
            "Critical configuration change detected for client {} with rule type {}",
            change.getClientId(),
            change.getRuleType()
        );
    }
}