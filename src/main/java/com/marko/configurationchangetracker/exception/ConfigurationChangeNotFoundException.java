package com.marko.configurationchangetracker.exception;

public class ConfigurationChangeNotFoundException extends RuntimeException {

    public ConfigurationChangeNotFoundException(long id) {
        super("Configuration change with id " + id + " was not found");
    }
}