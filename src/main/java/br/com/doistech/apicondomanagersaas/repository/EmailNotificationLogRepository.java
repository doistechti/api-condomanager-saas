package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.notificacao.EmailNotificationLog;
import br.com.doistech.apicondomanagersaas.domain.notificacao.EmailNotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailNotificationLogRepository extends JpaRepository<EmailNotificationLog, Long> {

    List<EmailNotificationLog> findTop50ByStatusOrderByCreatedAtDesc(EmailNotificationStatus status);
}
