package com.example.servicemonitor.domain;

public enum ErrorType {
    NONE,
    TIMEOUT,
    DNS_ERROR,
    CONNECTION_REFUSED,
    SSL_ERROR,
    HTTP_4XX,
    HTTP_5XX,
    INVALID_JSON,
    MISSING_STATUS,
    UNKNOWN_ERROR
}
