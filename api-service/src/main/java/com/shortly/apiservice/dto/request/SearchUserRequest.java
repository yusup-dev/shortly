package com.shortly.apiservice.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SearchUserRequest extends PaginationRequest {
    private String search;

    /** active | suspended */
    private String status;

    /** field:direction, e.g. created_at:desc */
    private String sort;
}
