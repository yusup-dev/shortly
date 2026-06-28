package com.shortly.apiservice.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SearchUrlRequest extends PaginationRequest {
    private String search;
}
