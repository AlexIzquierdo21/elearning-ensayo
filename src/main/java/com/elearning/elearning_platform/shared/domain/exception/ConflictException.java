package com.elearning.elearning_platform.shared.domain.exception;

/**
 * Domain exception thrown when an operation conflicts with the current state of a resource (e.g. a
 * uniqueness rule). Translated to HTTP 409 by the infrastructure layer.
 */
public class ConflictException extends DomainException {
    public ConflictException(String message) {
        super(message);
    }
}
