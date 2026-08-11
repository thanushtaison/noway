package com.noway.dto;

import jakarta.validation.constraints.Min;

public record UpdateCartRequest(
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {
}
