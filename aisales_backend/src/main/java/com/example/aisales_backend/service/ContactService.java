package com.example.aisales_backend.service;

import com.example.aisales_backend.dto.ContactRequest;
import com.example.aisales_backend.dto.ContactResponse;
import com.example.aisales_backend.entity.Contact;
import com.example.aisales_backend.repository.ContactRepository;
import com.example.aisales_backend.exception.EntityNotFoundException;
import com.example.aisales_backend.exception.DuplicateEntityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactResponse createContact(ContactRequest request, Long companyId) {
        log.info("Creating new contact: {} {} for company: {}", request.getFirstName(), request.getLastName(), companyId);

        // Check if email already exists within the same company (if provided)
        if (request.getEmail() != null && !request.getEmail().isEmpty() &&
            contactRepository.existsByEmailIgnoreCaseAndCompanyId(request.getEmail(), companyId)) {
            throw new DuplicateEntityException("Contact", "email", request.getEmail());
        }

        Contact contact = Contact.builder()
                .salutation(request.getSalutation())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .jobTitle(request.getJobTitle())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .department(request.getDepartment())
                .status(request.getStatus())
                .companyName(request.getCompanyName())
                .company(com.example.aisales_backend.entity.Company.builder().id(companyId).build())
                .build();

        Contact savedContact = contactRepository.save(contact);
        log.info("Contact created successfully: {} for company: {}", savedContact.getId(), companyId);

        return mapToContactResponse(savedContact);
    }

    public List<ContactResponse> getAllContacts(Long companyId) {
        log.info("Fetching all contacts for company: {}", companyId);
        return contactRepository.findByCompanyId(companyId).stream()
                .map(this::mapToContactResponse)
                .collect(Collectors.toList());
    }

    public ContactResponse getContactById(Long id, Long companyId) {
        log.info("Fetching contact by ID: {} for company: {}", id, companyId);
        Contact contact = contactRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Contact", id));
        return mapToContactResponse(contact);
    }


    public ContactResponse updateContact(Long id, ContactRequest request, Long companyId) {
        log.info("Updating contact with ID: {} for company: {}", id, companyId);
        Contact contact = contactRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Contact", id));

        // Check if email already exists within the same company (if provided and different from current)
        if (request.getEmail() != null && !request.getEmail().isEmpty() &&
            !request.getEmail().equalsIgnoreCase(contact.getEmail()) &&
            contactRepository.existsByEmailIgnoreCaseAndCompanyId(request.getEmail(), companyId)) {
            throw new DuplicateEntityException("Contact", "email", request.getEmail());
        }

        contact.setSalutation(request.getSalutation());
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setJobTitle(request.getJobTitle());
        contact.setEmail(request.getEmail());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setDepartment(request.getDepartment());
        contact.setStatus(request.getStatus());
        contact.setCompanyName(request.getCompanyName());

        Contact updatedContact = contactRepository.save(contact);
        log.info("Contact updated successfully: {} for company: {}", updatedContact.getId(), companyId);

        return mapToContactResponse(updatedContact);
    }

    public void deleteContact(Long id, Long companyId) {
        log.info("Deleting contact with ID: {} for company: {}", id, companyId);
        if (!contactRepository.findByIdAndCompanyId(id, companyId).isPresent()) {
            throw new EntityNotFoundException("Contact", id);
        }
        contactRepository.deleteById(id);
        log.info("Contact deleted successfully: {} for company: {}", id, companyId);
    }

    private ContactResponse mapToContactResponse(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .salutation(contact.getSalutation())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .jobTitle(contact.getJobTitle())
                .email(contact.getEmail())
                .phoneNumber(contact.getPhoneNumber())
                .department(contact.getDepartment())
                .status(contact.getStatus())
                .companyName(contact.getCompanyName())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }
}
