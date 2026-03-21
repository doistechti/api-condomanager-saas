package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.aws.AwsCredential;
import br.com.doistech.apicondomanagersaas.dto.aws.AwsCredentialResponse;
import br.com.doistech.apicondomanagersaas.dto.aws.AwsCredentialUpsertRequest;
import br.com.doistech.apicondomanagersaas.repository.AwsCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AwsCredentialService {

    private final AwsCredentialRepository repository;

    @Transactional
    public AwsCredentialResponse upsert(AwsCredentialUpsertRequest req) {
        var now = LocalDateTime.now();
        var entity = repository.findTopByOrderByIdAsc()
                .orElseGet(() -> AwsCredential.builder()
                        .createdAt(now)
                        .build());

        entity.setAccessKey(req.accessKey());
        entity.setSecretKey(req.secretKey());
        entity.setRegion(req.region());
        entity.setBucket(req.bucket());
        entity.setEndpoint(req.endpoint());
        entity.setPathStyleAccess(req.pathStyleAccess() == null ? Boolean.FALSE : req.pathStyleAccess());
        entity.setAtivo(req.ativo() == null ? Boolean.TRUE : req.ativo());
        entity.setUpdatedAt(now);

        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public AwsCredentialResponse getConfig() {
        return toResponse(getEntity());
    }

    @Transactional
    public void deleteConfig() {
        repository.delete(getEntity());
    }

    @Transactional(readOnly = true)
    public AwsCredential getEntity() {
        return repository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new NotFoundException("Configuração AWS não encontrada"));
    }

    private AwsCredentialResponse toResponse(AwsCredential entity) {
        return new AwsCredentialResponse(
                entity.getId(),
                entity.getAccessKey(),
                entity.getSecretKey(),
                entity.getRegion(),
                entity.getBucket(),
                entity.getEndpoint(),
                entity.getPathStyleAccess(),
                entity.getAtivo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
