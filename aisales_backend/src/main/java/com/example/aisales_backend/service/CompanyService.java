package com.example.aisales_backend.service;

import com.example.aisales_backend.dto.CompanyRequest;
import com.example.aisales_backend.dto.CompanyResponse;
import com.example.aisales_backend.entity.Company;
import com.example.aisales_backend.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        // Check if company already exists
        if (companyRepository.existsByCompanyName(request.getCompanyName())) {
            throw new RuntimeException("Company with this name already exists");
        }

        Company company = Company.builder()
                .companyName(request.getCompanyName())
                .industry(request.getIndustry())
                .address(request.getAddress())
                .build();

        Company savedCompany = companyRepository.save(company);
        log.info("New company created: {}", savedCompany.getCompanyName());

        return mapToCompanyResponse(savedCompany);
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    private CompanyResponse mapToCompanyResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .industry(company.getIndustry())
                .address(company.getAddress())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
