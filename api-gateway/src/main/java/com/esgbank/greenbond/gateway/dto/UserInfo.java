package com.esgbank.greenbond.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * User information DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private String userId;

    private String username;

    private String email;

    private List<String> roles;

    private String userType;
}