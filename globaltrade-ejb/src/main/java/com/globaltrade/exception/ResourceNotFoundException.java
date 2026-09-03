package com.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ResourceNotFoundException extends GlobalTradeException {

    private final String entityName;
    private final Object identifier;

    public ResourceNotFoundException(String entityName, Object identifier) {
        super(entityName + " record not found with identifier: " + identifier);
        this.entityName = entityName;
        this.identifier = identifier;
    }

    public String getEntityName() {
        return entityName;
    }

    public Object getIdentifier() {
        return identifier;
    }
}
