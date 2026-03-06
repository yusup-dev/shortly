package com.shortly.apiservice.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaginationResponse {
    private int currentPage;
    private int perPage;
    private int totalPages;
    private int totalItems;

    public static PaginationResponse fromPage(Page<?> page){
        PaginationResponse pagination = new PaginationResponse();
        pagination.setCurrentPage(page.getNumber() + 1);
        pagination.setPerPage(page.getSize());
        pagination.setTotalPages(page.getTotalPages());
        pagination.setTotalItems((int) page.getTotalElements());
        return pagination;
    }
}