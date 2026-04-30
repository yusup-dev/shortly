package com.shortly.kgsservice.model;

import com.shortly.kgsservice.enumaration.StatusType;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document(collection = "shortly_keys")
public class ShortlyKey {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String key;

    private StatusType status;

    private LocalDateTime createdAt;
}
