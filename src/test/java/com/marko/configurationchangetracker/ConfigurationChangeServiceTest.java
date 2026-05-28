package com.marko.configurationchangetracker;

import com.marko.configurationchangetracker.dto.ConfigurationChangeResponse;
import com.marko.configurationchangetracker.dto.ConfigurationChangeRequestBody;
import com.marko.configurationchangetracker.exception.ConfigurationChangeNotFoundException;
import com.marko.configurationchangetracker.exception.InvalidConfigurationChangeException;
import com.marko.configurationchangetracker.model.ChangeAction;
import com.marko.configurationchangetracker.model.ClientPriority;
import com.marko.configurationchangetracker.model.ConfigurationChange;
import com.marko.configurationchangetracker.model.RuleType;
import com.marko.configurationchangetracker.repository.ConfigurationChangeRepository;
import com.marko.configurationchangetracker.service.ConfigurationChangeService;
import com.marko.configurationchangetracker.service.MonitoringNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConfigurationChangeServiceTest {

    private final LocalDateTime createdAt = LocalDateTime.of(2026, 5, 27, 10, 0);

    private ConfigurationChange change;

    @Mock
    private ConfigurationChangeRepository repository;

    @Mock
    private MonitoringNotificationService monitoringNotificationService;

    @InjectMocks
    private ConfigurationChangeService service;

    @BeforeEach
    void setUp() {
        change = new ConfigurationChange();
        change.setId(1L);
        change.setClientId("client-123");
        change.setClientPriority(ClientPriority.HIGH);
        change.setRuleType(RuleType.CARD_LIMIT);
        change.setChangeAction(ChangeAction.UPDATE);
        change.setOldValue("5000");
        change.setNewValue("10000");
        change.setCritical(true);
        change.setCreatedAt(createdAt);
    }

    @Test
    void createChange_validUpdate_savesAndReturnsResponse() {
        ConfigurationChangeRequestBody requestBody = new ConfigurationChangeRequestBody(
            "client-123",
            ClientPriority.HIGH,
            RuleType.CARD_LIMIT,
            ChangeAction.UPDATE,
            "5000",
            "10000",
            true
        );

        when(repository.save(any(ConfigurationChange.class)))
            .thenAnswer(invocation -> {
                ConfigurationChange savedChange = invocation.getArgument(0);
                savedChange.setId(1L);
                return savedChange;
            });

        ConfigurationChangeResponse response = service.createChange(requestBody);

        assertEquals(1L, response.id());
        assertEquals("client-123", response.clientId());
        assertEquals(ClientPriority.HIGH, response.clientPriority());
        assertEquals(RuleType.CARD_LIMIT, response.ruleType());
        assertEquals(ChangeAction.UPDATE, response.changeAction());
        assertEquals("5000", response.oldValue());
        assertEquals("10000", response.newValue());
        assertTrue(response.critical());
        assertNotNull(response.createdAt());

        verify(repository, times(1)).save(any(ConfigurationChange.class));
        verify(monitoringNotificationService, times(1)).notifyCriticalChange(any(ConfigurationChange.class));
    }

    @Test
    void createChange_criticalChange_callsMonitoringNotificationService() {
        ConfigurationChangeRequestBody requestBody = new ConfigurationChangeRequestBody(
            "client-123",
            ClientPriority.HIGH,
            RuleType.CARD_LIMIT,
            ChangeAction.UPDATE,
            "5000",
            "10000",
            true
        );

        when(repository.save(any(ConfigurationChange.class)))
            .thenAnswer(invocation -> {
                ConfigurationChange savedChange = invocation.getArgument(0);
                savedChange.setId(2L);
                return savedChange;
            });

        service.createChange(requestBody);

        verify(monitoringNotificationService, times(1)).notifyCriticalChange(any(ConfigurationChange.class));
    }

    @Test
    void createChange_invalidAdd_throwsInvalidConfigurationChangeException() {
        ConfigurationChangeRequestBody requestBody = new ConfigurationChangeRequestBody(
            "client-123",
            ClientPriority.HIGH,
            RuleType.CARD_LIMIT,
            ChangeAction.ADD,
            "5000",
            null,
            true
        );

        assertThrows(InvalidConfigurationChangeException.class, () -> service.createChange(requestBody));
        verify(repository, never()).save(any(ConfigurationChange.class));
        verify(monitoringNotificationService, never()).notifyCriticalChange(any(ConfigurationChange.class));
    }

    @Test
    void createChange_invalidUpdate_throwsInvalidConfigurationChangeException() {
        ConfigurationChangeRequestBody requestBody = new ConfigurationChangeRequestBody(
            "client-123",
            ClientPriority.HIGH,
            RuleType.CARD_LIMIT,
            ChangeAction.UPDATE,
            null,
            "10000",
            true
        );

        assertThrows(InvalidConfigurationChangeException.class, () -> service.createChange(requestBody));
        verify(repository, never()).save(any(ConfigurationChange.class));
        verify(monitoringNotificationService, never()).notifyCriticalChange(any(ConfigurationChange.class));
    }

    @Test
    void createChange_invalidDelete_throwsInvalidConfigurationChangeException() {
        ConfigurationChangeRequestBody requestBody = new ConfigurationChangeRequestBody(
            "client-123",
            ClientPriority.HIGH,
            RuleType.CARD_LIMIT,
            ChangeAction.DELETE,
            null,
            "10000",
            true
        );

        assertThrows(InvalidConfigurationChangeException.class, () -> service.createChange(requestBody));
        verify(repository, never()).save(any(ConfigurationChange.class));
        verify(monitoringNotificationService, never()).notifyCriticalChange(any(ConfigurationChange.class));
    }

    @Test
    void getChangeById_existingId_returnsChangeResponse() {
        when(repository.getById(1L))
            .thenReturn(Optional.of(change));

        ConfigurationChangeResponse response = service.getChangeById(1L);

        assertEquals(1L, response.id());
        assertEquals("client-123", response.clientId());
        assertEquals(ClientPriority.HIGH, response.clientPriority());
        assertEquals(RuleType.CARD_LIMIT, response.ruleType());
        assertEquals(ChangeAction.UPDATE, response.changeAction());
        assertEquals("5000", response.oldValue());
        assertEquals("10000", response.newValue());
        assertTrue(response.critical());
        assertNotNull(response.createdAt());
    }

    @Test
    void getChangeById_missingId_throwsConfigurationChangeNotFoundException() {
        when(repository.getById(99L))
            .thenReturn(Optional.empty());

        assertThrows(ConfigurationChangeNotFoundException.class, () -> service.getChangeById(99L));
    }

    @Test
    void getChanges_withoutFilters_returnsAllChanges() {
        ConfigurationChange secondChange = new ConfigurationChange();
        secondChange.setId(2L);
        secondChange.setClientId("client-456");
        secondChange.setClientPriority(ClientPriority.MEDIUM);
        secondChange.setRuleType(RuleType.APPROVAL_POLICY);
        secondChange.setChangeAction(ChangeAction.ADD);
        secondChange.setNewValue("enabled");
        secondChange.setCritical(false);
        secondChange.setCreatedAt(createdAt.plusDays(1));

        when(repository.getAllChanges())
            .thenReturn(List.of(change, secondChange));

        List<ConfigurationChangeResponse> responses = service.getChanges(null, null, null, null, null);

        assertEquals(2, responses.size());
        assertEquals(1L, responses.getFirst().id());
        assertEquals(2L, responses.get(1).id());
    }

    @Test
    void getChanges_filterByRuleType_returnsFilteredChanges() {
        ConfigurationChange secondChange = new ConfigurationChange();
        secondChange.setId(2L);
        secondChange.setClientId("client-456");
        secondChange.setClientPriority(ClientPriority.MEDIUM);
        secondChange.setRuleType(RuleType.APPROVAL_POLICY);
        secondChange.setChangeAction(ChangeAction.ADD);
        secondChange.setNewValue("enabled");
        secondChange.setCritical(false);
        secondChange.setCreatedAt(createdAt.plusDays(1));

        when(repository.getAllChanges())
            .thenReturn(List.of(change, secondChange));

        List<ConfigurationChangeResponse> responses = service.getChanges(
            null,
            RuleType.CARD_LIMIT,
            null,
            null,
            null
        );

        assertEquals(1, responses.size());
        assertEquals(1L, responses.getFirst().id());
        assertEquals(RuleType.CARD_LIMIT, responses.getFirst().ruleType());
    }

    @Test
    void getChanges_filterByDate_returnsFilteredChanges() {
        ConfigurationChange olderChange = new ConfigurationChange();
        olderChange.setId(2L);
        olderChange.setClientId("client-456");
        olderChange.setClientPriority(ClientPriority.MEDIUM);
        olderChange.setRuleType(RuleType.APPROVAL_POLICY);
        olderChange.setChangeAction(ChangeAction.ADD);
        olderChange.setNewValue("enabled");
        olderChange.setCritical(false);
        olderChange.setCreatedAt(createdAt.minusDays(2));

        ConfigurationChange inRangeChange = new ConfigurationChange();
        inRangeChange.setId(3L);
        inRangeChange.setClientId("client-789");
        inRangeChange.setClientPriority(ClientPriority.LOW);
        inRangeChange.setRuleType(RuleType.CARD_LIMIT);
        inRangeChange.setChangeAction(ChangeAction.UPDATE);
        inRangeChange.setOldValue("1000");
        inRangeChange.setNewValue("1500");
        inRangeChange.setCritical(false);
        inRangeChange.setCreatedAt(createdAt);

        when(repository.getAllChanges())
            .thenReturn(List.of(olderChange, inRangeChange));

        List<ConfigurationChangeResponse> responses = service.getChanges(
            null,
            null,
            null,
            LocalDate.of(2026, 5, 27),
            LocalDate.of(2026, 5, 27)
        );

        assertEquals(1, responses.size());
        assertEquals(3L, responses.getFirst().id());
    }


}
