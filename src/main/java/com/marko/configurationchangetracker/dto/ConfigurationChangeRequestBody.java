package com.marko.configurationchangetracker.dto;

import com.marko.configurationchangetracker.model.ChangeAction;
import com.marko.configurationchangetracker.model.ClientPriority;
import com.marko.configurationchangetracker.model.RuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record ConfigurationChangeRequestBody(

    @NotBlank
    String clientId,

    @NotNull
    ClientPriority clientPriority,

    @NotNull
    RuleType ruleType,

    @NotNull
    ChangeAction changeAction,

    @NotBlank
    String oldValue,

    @NotBlank
    String newValue,

    @NotNull
    boolean critical
    ) {
}
