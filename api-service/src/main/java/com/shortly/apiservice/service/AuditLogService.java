package com.shortly.apiservice.service;

import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.enumaration.ActionType;
import com.shortly.apiservice.enumaration.TargetType;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public interface AuditLogService {
    String getClientIpAddress(HttpServletRequest servletRequest);
    void saveAuditLog(HttpServletRequest servletRequest,
                      ActionType actionType,
                      TargetType targetType,
                      UUID target_id);
}
