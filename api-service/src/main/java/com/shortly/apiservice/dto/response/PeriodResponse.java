package com.shortly.apiservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class PeriodResponse {
    private LocalDate from;
    private LocalDate to;
}
