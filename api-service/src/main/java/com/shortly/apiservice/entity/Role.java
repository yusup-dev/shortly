package com.shortly.apiservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import com.shortly.apiservice.enumaration.RoleType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 20, nullable = false)
    private RoleType name;

    @Column(name = "description", length = 255, nullable = false)
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "role")
    private List<User> users;
}