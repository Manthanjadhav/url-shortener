package com.manthan.urlShortener.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ClickEventDTO {
    private LocalDate clickDate;
    private Long count;
}
