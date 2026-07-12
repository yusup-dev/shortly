package com.shortly.apiservice.dto;

import com.shortly.apiservice.enumaration.StatusType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserInfoTest {

    @Test
    void isEnabled_activeStatus_returnsTrue() {
        UserInfo userInfo = UserInfo.builder()
                .status(StatusType.ACTIVE.name())
                .build();

        assertThat(userInfo.isEnabled()).isTrue();
    }

    @Test
    void isEnabled_suspendedStatus_returnsFalse() {
        UserInfo userInfo = UserInfo.builder()
                .status(StatusType.SUSPENDED.name())
                .build();

        assertThat(userInfo.isEnabled()).isFalse();
    }

    @Test
    void isEnabled_nullStatus_returnsFalse() {
        UserInfo userInfo = UserInfo.builder().build();

        assertThat(userInfo.isEnabled()).isFalse();
    }
}
