package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.aws.AwsCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AwsCredentialRepository extends JpaRepository<AwsCredential, Long> {

    Optional<AwsCredential> findTopByOrderByIdAsc();
}
