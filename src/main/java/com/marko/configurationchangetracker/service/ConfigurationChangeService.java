package com.marko.configurationchangetracker.service;

import com.marko.configurationchangetracker.dto.ConfigurationChangeRequestBody;
import com.marko.configurationchangetracker.dto.ConfigurationChangeResponse;
import com.marko.configurationchangetracker.exception.ConfigurationChangeNotFoundException;
import com.marko.configurationchangetracker.exception.InvalidConfigurationChangeException;
import com.marko.configurationchangetracker.model.ChangeAction;
import com.marko.configurationchangetracker.model.ClientPriority;
import com.marko.configurationchangetracker.model.ConfigurationChange;
import com.marko.configurationchangetracker.model.RuleType;
import com.marko.configurationchangetracker.repository.ConfigurationChangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ConfigurationChangeService {

    private final ConfigurationChangeRepository repository;
    private final MonitoringNotificationService monitoringNotificationService;

    public ConfigurationChangeResponse createChange(ConfigurationChangeRequestBody requestBody) {
        validateAction(requestBody);
        ConfigurationChange change = requestToModel(requestBody);
        change.setCreatedAt(LocalDateTime.now());
        repository.save(change);

        if (change.isCritical()) {
            monitoringNotificationService.notifyCriticalChange(change);
        }

        return modelToResponse(change);
    }

    public List<ConfigurationChangeResponse> getChanges(
        ClientPriority priority,
        RuleType ruleType,
        ChangeAction action,
        LocalDate from,
        LocalDate to
        ) {

        return repository.getAllChanges()
            .stream()
            .filter(ch -> priority == null || ch.getClientPriority() == priority)
            .filter(ch -> ruleType == null || ch.getRuleType() == ruleType)
            .filter(ch -> action == null || ch.getChangeAction() == action)
            .filter(ch -> from == null || !ch.getCreatedAt().isBefore(from.atStartOfDay()))
            .filter(ch -> to == null || ch.getCreatedAt().isBefore(to.plusDays(1).atStartOfDay()))
            .map(this::modelToResponse)
            .toList();
    }

    public ConfigurationChangeResponse getChangeById(long id) {
        ConfigurationChange change = repository.getById(id)
            .orElseThrow(() ->
                new ConfigurationChangeNotFoundException(id)
            );
        return modelToResponse(change);
    }

    private ConfigurationChangeResponse modelToResponse(
        ConfigurationChange change
    ) {
        return new ConfigurationChangeResponse(
            change.getId(),
            change.getClientId(),
            change.getClientPriority(),
            change.getRuleType(),
            change.getChangeAction(),
            change.getOldValue(),
            change.getNewValue(),
            change.isCritical(),
            change.getCreatedAt()
        );
    }

    private ConfigurationChange requestToModel(ConfigurationChangeRequestBody requestBody) {
        ConfigurationChange change = new ConfigurationChange();
        change.setClientId(requestBody.clientId());
        change.setClientPriority(requestBody.clientPriority());
        change.setRuleType(requestBody.ruleType());
        change.setChangeAction(requestBody.changeAction());
        change.setOldValue(requestBody.oldValue());
        change.setNewValue(requestBody.newValue());
        change.setCritical(requestBody.critical());

        return change;
    }

    private void validateAction(ConfigurationChangeRequestBody requestBody) {

        String oldValue = requestBody.oldValue();
        String newValue = requestBody.newValue();

        boolean valid = switch (requestBody.changeAction()) {
            case ADD -> oldValue == null && newValue != null;
            case UPDATE -> oldValue != null && newValue != null;
            case DELETE -> oldValue != null && newValue == null;
        };

        if (!valid) {
            throw new InvalidConfigurationChangeException(
                "Invalid values for changeAction type " + requestBody.changeAction()
            );
        }
    }
}
