package com.shortly.apiservice.repository.projection;

import java.util.UUID;

public interface UserAuthProjection {
    UUID getId();
    String getEmail();
    String getPassword();
    String getStatus();
    String getRoleName();
    String getPlanName();
}
