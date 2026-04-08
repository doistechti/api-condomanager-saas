package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.assinatura.AssinaturaStatus;
import br.com.doistech.apicondomanagersaas.repository.AssinaturaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class AssinaturaReminderScheduler {

    private final AssinaturaRepository assinaturaRepository;
    private final AssinaturaEmailService assinaturaEmailService;

    public AssinaturaReminderScheduler(
            AssinaturaRepository assinaturaRepository,
            AssinaturaEmailService assinaturaEmailService
    ) {
        this.assinaturaRepository = assinaturaRepository;
        this.assinaturaEmailService = assinaturaEmailService;
    }

    @Transactional(readOnly = true)
    @Scheduled(cron = "${app.assinatura.reminder-cron:0 0 8 * * *}", zone = "America/Sao_Paulo")
    public void notifyDueDates() {
        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(Math.max(assinaturaEmailService.getReminderDays(), 1));

        assinaturaRepository.findAllByStatusAndDataVencimento(AssinaturaStatus.ATIVO, reminderDate)
                .forEach(assinaturaEmailService::sendDueSoonNotification);

        assinaturaRepository.findAllByStatusAndDataVencimentoBefore(AssinaturaStatus.ATIVO, today)
                .forEach(assinaturaEmailService::sendOverdueNotification);
    }
}
