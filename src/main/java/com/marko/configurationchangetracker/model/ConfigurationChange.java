package com.marko.configurationchangetracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationChange {

    private Long id;

    private String clientId;

    private ClientPriority clientPriority;

    private RuleType ruleType;

    private ChangeAction changeAction;

    private String oldValue;

    private String newValue;

    private boolean critical;

    private LocalDateTime createdAt;
}