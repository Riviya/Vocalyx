package com.example.aisales_backend.service.implement;

import com.example.aisales_backend.dto.history.CallDto;
import com.example.aisales_backend.entity.Call;
import com.example.aisales_backend.entity.Contact;
import com.example.aisales_backend.repository.CallRepository;
import com.example.aisales_backend.service.interfaces.ICallService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CallHistory implements ICallService {

    private final CallRepository callRepository;

    @Override
    public List<CallDto> getAllCallsLatestFirst() {
        List<Call> calls = callRepository.findAllWithContactOrderByCallDateTimeDesc();
        List<CallDto> result = new ArrayList<>();

        for (Call call : calls) {
            CallDto dto = new CallDto();

            dto.setId(call.getId());
            dto.setCallTitle(call.getCallTitle());
            dto.setCallDateTime(call.getCallDateTime());
            dto.setCallDirection(call.getCallDirection().name());
            dto.setSummary(call.getSummary());
            dto.setSentimentScore(call.getSentimentPercentage());
            dto.setSentimentType(call.getSentimentLabel() != null ? call.getSentimentLabel() : null);

            // Add order number mapping
            if (call.getOrder() != null) {
                dto.setOrderId(call.getOrder().getId());  // Set the Order's ID
            }

            Contact contact = call.getContact();
            if (contact != null) {
                dto.setFirstName(contact.getFirstName());
                dto.setLastName(contact.getLastName());
                dto.setEmail(contact.getEmail());
                dto.setPhoneNumber(contact.getPhoneNumber());
            }

            result.add(dto);
        }

        return result;
    }
}