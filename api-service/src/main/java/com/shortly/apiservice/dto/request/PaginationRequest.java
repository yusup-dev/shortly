package com.shortly.apiservice.dto.request;

import lombok.Data;

@Data
public class PaginationRequest {
    private Integer page = 1;
    private Integer limit = 10;
}
