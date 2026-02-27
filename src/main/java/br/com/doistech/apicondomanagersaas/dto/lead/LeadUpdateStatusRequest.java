package br.com.doistech.apicondomanagersaas.dto.lead;

import br.com.doistech.apicondomanagersaas.domain.lead.LeadStatus;
import jakarta.validation.constraints.NotNull;

public record LeadUpdateStatusRequest(
        @NotNull LeadStatus status
) {}

