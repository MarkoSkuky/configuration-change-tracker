package com.marko.configurationchangetracker.controller;

import com.marko.configurationchangetracker.dto.ConfigurationChangeRequestBody;
import com.marko.configurationchangetracker.dto.ConfigurationChangeResponse;
import com.marko.configurationchangetracker.model.ChangeAction;
import com.marko.configurationchangetracker.model.ClientPriority;
import com.marko.configurationchangetracker.model.RuleType;
import com.marko.configurationchangetracker.service.ConfigurationChangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/configuration-changes")
@RequiredArgsConstructor
public class ConfigurationChangeController {
    private final ConfigurationChangeService configurationChangeService;

    @GetMapping
    public List<ConfigurationChangeResponse> getChanges(
        @RequestParam(required = false)
        ClientPriority priority,

        @RequestParam(required = false)
        RuleType ruleType,

        @RequestParam(required = false)
        ChangeAction action,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to
    ) {

        return configurationChangeService.getChanges(priority, ruleType, action, from, to);
    }

    @GetMapping("/{id}")
    public ConfigurationChangeResponse getChangeById(
        @PathVariable
        long id
    ) {
        return configurationChangeService.getChangeById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConfigurationChangeResponse createChange(
        @Valid
        @RequestBody
        ConfigurationChangeRequestBody requestBody
    ) {
        return configurationChangeService.createChange(requestBody);
    }
}
