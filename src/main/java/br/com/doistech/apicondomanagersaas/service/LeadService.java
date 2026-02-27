package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.lead.Lead;
import br.com.doistech.apicondomanagersaas.domain.lead.LeadStatus;
import br.com.doistech.apicondomanagersaas.domain.role.Role;
import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.assinatura.Assinatura;
import br.com.doistech.apicondomanagersaas.domain.assinatura.AssinaturaStatus;
import br.com.doistech.apicondomanagersaas.dto.lead.LeadCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.lead.LeadResponse;
import br.com.doistech.apicondomanagersaas.dto.lead.LeadUpdateStatusRequest;
import br.com.doistech.apicondomanagersaas.repository.AssinaturaRepository;
import br.com.doistech.apicondomanagersaas.repository.CondominioRepository;
import br.com.doistech.apicondomanagersaas.repository.LeadRepository;
import br.com.doistech.apicondomanagersaas.repository.PlanoRepository;
import br.com.doistech.apicondomanagersaas.repository.RoleRepository;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CondominioRepository condominioRepository;
    private final PlanoRepository planoRepository;
    private final AssinaturaRepository assinaturaRepository;

    /**
     * Duração padrão do Trial (dias). Mantido simples e configurável.
     */
    @Value("${app.trial.dias:14}")
    private int trialDias;

    @Transactional
    public LeadResponse createPublic(LeadCreateRequest req) {
        Set<Role> roleSet = new HashSet<>();
        String email = normalizeEmail(req.email());

        // 1) não permitir e-mail já cadastrado como usuário
        if (usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException("Já existe um usuário cadastrado com este e-mail.");
        }

        // 2) cria o LEAD
        Lead entity = Lead.builder()
                .nomeCondominio(trim(req.nomeCondominio()))
                .cnpj(trim(req.cnpj()))
                .responsavel(trim(req.responsavel()))
                .email(email)
                .telefone(trim(req.telefone()))
                .unidadesEstimadas(req.unidadesEstimadas())
                .mensagem(trim(req.mensagem()))
                .status(LeadStatus.NOVO)
                .build();

        Lead savedLead = repository.save(entity);

        // 3) cria usuário ADMIN_CONDOMINIO pendente (ativo=false)
        var roleAdminCondo = roleRepository.findByNome("ADMIN_CONDOMINIO")
                .orElseThrow(() -> new IllegalStateException("Role ADMIN_CONDOMINIO não encontrada. Rode o bootstrap de roles."));

        if(roleAdminCondo != null){
            roleSet.add(roleAdminCondo);
        }

        Usuario usuario = Usuario.builder()
                .nome(trim(req.responsavel()))
                .email(email)
                .senha(passwordEncoder.encode(req.senha()))
                .ativo(false)
                .condominioId(null) // só será vinculado após liberar trial
                .build();
        usuario.setRoles(roleSet);
        usuarioRepository.save(usuario);

        return toResponse(savedLead);
    }

    /**
     * Libera Trial para um lead:
     * - atualiza status do lead
     * - cria condomínio
     * - cria assinatura (trial) com datas
     * - ativa usuário e vincula ao condomínio
     */
    @Transactional
    public LeadResponse liberarTrial(Long leadId) {
        Lead lead = getEntity(leadId);

        if (lead.getStatus() == LeadStatus.TRIAL_LIBERADO || lead.getStatus() == LeadStatus.CONVERTIDO) {
            // idempotente: se já liberado/convertido, só devolve o estado atual
            return toResponse(lead);
        }

        Usuario usuario = usuarioRepository.findByEmail(lead.getEmail())
                .orElseThrow(() -> new NotFoundException("Usuário pendente não encontrado para o lead: " + leadId));

        // cria condomínio baseado no lead
        Condominio condominio = Condominio.builder()
                .nome(lead.getNomeCondominio())
                .cnpj(lead.getCnpj())
                .responsavel(lead.getResponsavel())
                .email(lead.getEmail())
                .telefone(lead.getTelefone())
                .build();

        // plano padrão usado no trial (mantido simples)
        var plano = planoRepository.findByNome("Essencial")
                .orElseThrow(() -> new IllegalStateException("Plano 'Essencial' não encontrado. Rode o bootstrap de planos."));

        condominio.setPlano(plano);
        Condominio savedCondominio = condominioRepository.save(condominio);

        // cria assinatura trial
        LocalDate inicio = LocalDate.now();
        LocalDate fim = inicio.plusDays(Math.max(trialDias, 1));

        Assinatura assinatura = Assinatura.builder()
                .condominio(savedCondominio)
                .plano(plano)
                .status(AssinaturaStatus.ATIVO)
                .dataInicio(inicio)
                .dataVencimento(fim)
                .mercadoPagoId(null)
                .build();

        assinaturaRepository.save(assinatura);

        // ativa usuário e vincula ao condomínio
        usuario.setAtivo(true);
        usuario.setCondominioId(savedCondominio.getId());
        usuarioRepository.save(usuario);

        // atualiza lead
        lead.setStatus(LeadStatus.TRIAL_LIBERADO);
        Lead savedLead = repository.save(lead);

        return toResponse(savedLead);
    }

    @Transactional(readOnly = true)
    public Page<LeadResponse> list(LeadStatus status, Pageable pageable) {
        Page<Lead> page = (status == null)
                ? repository.findAll(pageable)
                : repository.findByStatus(status, pageable);

        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public LeadResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public LeadResponse updateStatus(Long id, LeadUpdateStatusRequest req) {
        Lead entity = getEntity(id);
        entity.setStatus(req.status());
        Lead saved = repository.save(entity);
        return toResponse(saved);
    }

    private Lead getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lead não encontrado: " + id));
    }

    private LeadResponse toResponse(Lead entity) {
        return new LeadResponse(
                entity.getId(),
                entity.getNomeCondominio(),
                entity.getCnpj(),
                entity.getResponsavel(),
                entity.getEmail(),
                entity.getTelefone(),
                entity.getUnidadesEstimadas(),
                entity.getMensagem(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    private String trim(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }
}

