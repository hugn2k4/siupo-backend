package com.siupo.restaurant.service.combo;

import com.siupo.restaurant.dto.request.CreateComboRequest;
import com.siupo.restaurant.dto.response.ComboResponse;
import com.siupo.restaurant.model.Combo;

import java.util.List;

public interface ComboService {
    ComboResponse createCombo(CreateComboRequest request);
    ComboResponse getComboById(Long id);
    List<ComboResponse> getAllCombos();
    List<ComboResponse> getAvailableCombos();
    ComboResponse updateCombo(Long id, CreateComboRequest request);
    void deleteCombo(Long id);
    ComboResponse toggleComboStatus(Long id);
}

