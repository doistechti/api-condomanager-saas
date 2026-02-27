package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.lead.Lead;
import br.com.doistech.apicondomanagersaas.domain.lead.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    Page<Lead> findByStatus(LeadStatus status, Pageable pageable);
}

