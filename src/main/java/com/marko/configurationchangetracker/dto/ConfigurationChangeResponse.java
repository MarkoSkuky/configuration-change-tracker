package com.marko.configurationchangetracker.dto;

import com.marko.configurationchangetracker.model.ChangeAction;
import com.marko.configurationchangetracker.model.ClientPriority;
import com.marko.configurationchangetracker.model.RuleType;

import java.time.LocalDateTime;

public record ConfigurationChangeResponse(

    Long id,

    String clientId,

    ClientPriority clientPriority,

    RuleType ruleType,

    ChangeAction changeAction,

    String oldValue,

    String newValue,

    boolean critical,

    LocalDateTime createdAt
) {
}