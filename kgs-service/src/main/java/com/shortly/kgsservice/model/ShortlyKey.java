package com.shortly.kgsservice.model;

import com.shortly.kgsservice.enumaration.StatusType;
import jakarta.persistence.Id;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document(collection = "shortly_keys")
public class ShortlyKey {

    @Id
    private ObjectId id;

    private String key;

    private StatusType status;

    private LocalDateTime createdAt;
}
