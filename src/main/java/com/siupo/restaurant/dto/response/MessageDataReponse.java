package com.siupo.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDataReponse {
    private boolean success;
    private String code;
    private String message;
    @Builder.Default
    private Object data = null;
    public MessageDataReponse(boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = null;
    }
}
