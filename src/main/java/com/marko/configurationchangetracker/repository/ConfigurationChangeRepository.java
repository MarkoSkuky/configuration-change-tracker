package com.marko.configurationchangetracker.repository;

import com.marko.configurationchangetracker.model.ConfigurationChange;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ConfigurationChangeRepository {

    private final Map<Long, ConfigurationChange> changes = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public List<ConfigurationChange> getAllChanges() {
        return changes.values().stream().toList();
    }

    public Optional<ConfigurationChange> getById(Long id) {
        return Optional.ofNullable(changes.get(id));
    }

    public ConfigurationChange save(ConfigurationChange change) {
        Long id = idGenerator.getAndIncrement();
        change.setId(id);
        changes.put(id, change);
        return change;
    }

    public void deleteById(Long id) {
        changes.remove(id);
    }


}
