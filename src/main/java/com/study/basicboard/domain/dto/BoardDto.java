package com.study.basicboard.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BoardDto {

    private String title;
    private String body;


}
