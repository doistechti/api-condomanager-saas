package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.chat.ConversaTipo;
import br.com.doistech.apicondomanagersaas.domain.chat.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    List<Mensagem> findAllByConversaIdOrderByCreatedAtAsc(Long conversaId);

    long countByConversaIdAndLidaFalseAndRemetenteIdNot(Long conversaId, Long remetenteIdNot);

    @Modifying
    @Query("""
        update Mensagem m
           set m.lida = true
         where m.conversa.id = :conversaId
           and m.lida = false
           and m.remetente.id <> :excludeRemetenteId
    """)
    int markAsRead(Long conversaId, Long excludeRemetenteId);

    @Query("""
        select count(distinct m.conversa.id)
          from Mensagem m
         where m.conversa.condominio.id = :condominioId
           and (:tipo is null or m.conversa.tipo = :tipo)
           and m.lida = false
           and m.remetente.id <> :uid
    """)
    long countConversasComNaoLidas(Long condominioId, ConversaTipo tipo, Long uid);
}