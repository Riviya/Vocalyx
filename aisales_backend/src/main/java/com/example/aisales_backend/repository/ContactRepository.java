package com.example.aisales_backend.repository;

import com.example.aisales_backend.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByDepartment(Contact.Department department);

    Optional<Contact> findByEmailIgnoreCase(String email);

    @Query("SELECT c FROM Contact c WHERE c.firstName LIKE %:name% OR c.lastName LIKE %:name%")
    List<Contact> findByNameContainingIgnoreCase(@Param("name") String name);

    boolean existsByEmailIgnoreCase(String email);

    List<Contact> findByCompanyId(Long companyId);

    Optional<Contact> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByEmailIgnoreCaseAndCompanyId(String email, Long companyId);
}
