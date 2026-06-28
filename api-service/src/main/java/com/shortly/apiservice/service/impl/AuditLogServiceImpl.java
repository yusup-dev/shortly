package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.entity.AuditLog;
import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.enumaration.ActionType;
import com.shortly.apiservice.enumaration.TargetType;
import com.shortly.apiservice.repository.AuditLogRepository;
import com.shortly.apiservice.service.AuditLogService;
import com.shortly.apiservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserService userService;

    @Override
    public String getClientIpAddress(HttpServletRequest servletRequest) {
        String xfHeader = servletRequest.getHeader("X-Forwarder-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return servletRequest.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    @Override
    public void saveAuditLog(HttpServletRequest servletRequest,
                             ActionType actionType,
                             TargetType targetType,
                             UUID targetId) {

        User currentUser = userService.getCurrentUser();
        String ipAddress = getClientIpAddress(servletRequest);

        AuditLog auditLog = AuditLog.builder()
                .actorType(currentUser.getRole().getName())
                .actorId(currentUser.getId())
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(auditLog);
    }
}
