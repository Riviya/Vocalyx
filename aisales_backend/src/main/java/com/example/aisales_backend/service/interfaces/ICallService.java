package com.example.aisales_backend.service.interfaces;

import com.example.aisales_backend.dto.history.CallDto;
import java.util.List;

public interface ICallService {

    /**
     * Retrieve all calls, latest first
     */
    List<CallDto> getAllCallsLatestFirst();
}