package com.shortly.apiservice.dto.request;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BulkUrlRequest {
    @NotEmpty(message = "urls wajib diisi")
    @Valid
    private List<BulkUrlItemRequest> urls;
}
