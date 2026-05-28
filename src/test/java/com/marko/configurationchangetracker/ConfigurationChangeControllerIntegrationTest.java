package com.marko.configurationchangetracker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marko.configurationchangetracker.dto.ConfigurationChangeRequestBody;
import com.marko.configurationchangetracker.dto.ConfigurationChangeResponse;
import com.marko.configurationchangetracker.model.ChangeAction;
import com.marko.configurationchangetracker.model.ClientPriority;
import com.marko.configurationchangetracker.model.RuleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class ConfigurationChangeControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private ConfigurationChangeRequestBody validRequestBody;

    @BeforeEach
    void setUp() {
        validRequestBody = new ConfigurationChangeRequestBody(
            "client123",
            ClientPriority.MEDIUM,
            RuleType.DAILY_TRANSFER_LIMIT,
            ChangeAction.UPDATE,
            "10000",
            "15000",
            true
        );
    }

    @Test
    void createChange_validChange_returnsChangeResponse() throws Exception {

        mockMvc.perform(post("/configuration-changes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestBody)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.clientId").value("client123"))
            .andExpect(jsonPath("$.clientPriority").value("MEDIUM"))
            .andExpect(jsonPath("$.ruleType").value("DAILY_TRANSFER_LIMIT"))
            .andExpect(jsonPath("$.changeAction").value("UPDATE"))
            .andExpect(jsonPath("$.oldValue").value("10000"))
            .andExpect(jsonPath("$.newValue").value("15000"))
            .andExpect(jsonPath("$.critical").value(true));
    }

    @Test
    void createChange_invalidChange_returnsError400() throws Exception {
        ConfigurationChangeRequestBody requestBody = new ConfigurationChangeRequestBody(
            "client123",
            ClientPriority.MEDIUM,
            RuleType.DAILY_TRANSFER_LIMIT,
            ChangeAction.ADD,
            "10000",
            "15000",
            true
        );

        mockMvc.perform(post("/configuration-changes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message")
            .value("Invalid values for changeAction type ADD"));
    }

    @Test
    void getChanges_withoutFilters_returnsStatus200() throws Exception {

        mockMvc.perform(post("/configuration-changes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequestBody)));

        mockMvc.perform(get("/configuration-changes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].clientId").value("client123"));
    }

    @Test
    void getChangeById_existingId_returnsChange() throws Exception{

        String response = mockMvc.perform(post("/configuration-changes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestBody)))
            .andReturn()
            .getResponse()
            .getContentAsString();

        ConfigurationChangeResponse createdResponse =
            objectMapper.readValue(response, ConfigurationChangeResponse.class);

        Long id = createdResponse.id();

        mockMvc.perform(get("/configuration-changes/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.clientId").value("client123"));
    }

    @Test
    void getChangeById_missingId_returnsError404() throws Exception {
        mockMvc.perform(get("/configuration-changes/999999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getChanges_filterByRuleType_returnsFilteredChanges() throws Exception {
        ConfigurationChangeRequestBody cardLimitRequest = validRequestBody;

        ConfigurationChangeRequestBody approvalPolicyRequest = new ConfigurationChangeRequestBody(
            "client456",
            ClientPriority.HIGH,
            RuleType.APPROVAL_POLICY,
            ChangeAction.ADD,
            null,
            "MANAGER_APPROVAL_REQUIRED",
            false
        );

        mockMvc.perform(post("/configuration-changes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cardLimitRequest)));

        mockMvc.perform(post("/configuration-changes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(approvalPolicyRequest)));

        mockMvc.perform(get("/configuration-changes?ruleType=DAILY_TRANSFER_LIMIT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].ruleType").value("DAILY_TRANSFER_LIMIT"));
    }

    @Test
    void createChange_blankClientId_returnsError400() throws Exception {
        ConfigurationChangeRequestBody requestBody = new ConfigurationChangeRequestBody(
            "",
            ClientPriority.MEDIUM,
            RuleType.DAILY_TRANSFER_LIMIT,
            ChangeAction.UPDATE,
            "10000",
            "15000",
            true
        );

        mockMvc.perform(post("/configuration-changes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getChanges_invalidEnumParameter_returnsError400() throws Exception {
        mockMvc.perform(get("/configuration-changes?priority=EXTREME"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").exists());
    }
}
