package com.shortly.apiservice.enumaration;

public enum AuditActionType {
    LOGIN,
    LOGIN_FAILED,
    CREATE_SHORT_URL,
    DELETE_SHORT_URL,
    UPDATE_QUOTA,
    CREATE_API_KEY,
    REVOKE_API_KEY,
    SUSPEND_USER
}
