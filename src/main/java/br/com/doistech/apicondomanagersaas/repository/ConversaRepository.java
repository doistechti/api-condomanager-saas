package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.chat.Conversa;
import br.com.doistech.apicondomanagersaas.domain.chat.ConversaStatus;
import br.com.doistech.apicondomanagersaas.domain.chat.ConversaTipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversaRepository extends JpaRepository<Conversa, Long> {

    Optional<Conversa> findByIdAndCondominioId(Long id, Long condominioId);

    List<Conversa> findAllByCondominioIdOrderByUltimaMensagemAtDesc(Long condominioId);

    List<Conversa> findAllByCondominioIdAndTipoOrderByUltimaMensagemAtDesc(Long condominioId, ConversaTipo tipo);

    List<Conversa> findAllByCondominioIdAndStatusOrderByUltimaMensagemAtDesc(Long condominioId, ConversaStatus status);

    List<Conversa> findAllByCondominioIdAndTipoAndStatusOrderByUltimaMensagemAtDesc(Long condominioId, ConversaTipo tipo, ConversaStatus status);
}