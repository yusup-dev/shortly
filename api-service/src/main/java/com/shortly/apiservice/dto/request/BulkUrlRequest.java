package com.shortly.apiservice.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class BulkUrlRequest {
    private List<BulkUrlItemRequest> urls;
}
