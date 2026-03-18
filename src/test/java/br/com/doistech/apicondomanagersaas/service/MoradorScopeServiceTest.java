package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.domain.pessoa.Pessoa;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.domain.role.Role;
import br.com.doistech.apicondomanagersaas.domain.unidade.Unidade;
import br.com.doistech.apicondomanagersaas.domain.usuario.Usuario;
import br.com.doistech.apicondomanagersaas.dto.morador.MoradorScopeResponse;
import br.com.doistech.apicondomanagersaas.repository.PessoaUnidadeRepository;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import br.com.doistech.apicondomanagersaas.repository.VinculoUnidadeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoradorScopeServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PessoaUnidadeRepository pessoaUnidadeRepository;

    @Mock
    private VinculoUnidadeRepository vinculoUnidadeRepository;

    @Mock
    private CondominioService condominioService;

    @InjectMocks
    private MoradorScopeService service;

    @Test
    void shouldFallbackToPessoaUnidadeIdWhenOperationalVinculoFails() {
        Usuario usuario = Usuario.builder()
                .id(11L)
                .nome("Morador Teste")
                .email("morador@teste.com")
                .condominioId(7L)
                .roles(Set.of(Role.builder().nome("MORADOR").build()))
                .build();

        Condominio condominio = Condominio.builder()
                .id(7L)
                .nome("Condominio Teste")
                .build();

        Pessoa pessoa = Pessoa.builder()
                .id(13L)
                .nome("Pessoa Teste")
                .build();

        Unidade unidade = Unidade.builder()
                .id(17L)
                .identificacao("A-101")
                .ativo(true)
                .build();

        PessoaUnidade vinculo = new PessoaUnidade();
        vinculo.setId(19L);
        vinculo.setUsuario(usuario);
        vinculo.setCondominio(condominio);
        vinculo.setPessoa(pessoa);
        vinculo.setUnidade(unidade);
        vinculo.setEhMorador(true);
        vinculo.setAtivo(true);
        vinculo.setPrincipal(true);

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(pessoaUnidadeRepository.findAllByUsuarioIdAndEhMoradorTrueAndAtivoTrue(usuario.getId()))
                .thenReturn(List.of(vinculo));
        when(condominioService.getEntity(condominio.getId())).thenReturn(condominio);
        when(vinculoUnidadeRepository.findByCondominioIdAndPessoaIdAndUnidadeId(
                condominio.getId(),
                pessoa.getId(),
                unidade.getId()
        )).thenThrow(new RuntimeException("db failure"));

        MoradorScopeResponse response = service.getScope(usuario.getEmail());

        assertEquals(vinculo.getId(), response.vinculoPrincipalId());
        assertEquals(List.of(unidade.getId()), response.unidadeIds());
        assertEquals(pessoa.getId(), response.pessoaId());
    }
}
