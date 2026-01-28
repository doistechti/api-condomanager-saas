package br.com.doistech.apicondomanagersaas.dto.plano;

import java.math.BigDecimal;
import java.util.List;

public record PlanoResponse(
        Long id,
        String nome,
        BigDecimal preco,
        Integer maxUnidades,
        Integer maxAdmins,      // API devolve 999 para ilimitado (pra casar com o front)
        String descricao,
        List<String> recursos,
        Boolean destaque,
        Boolean ativo
) {}

