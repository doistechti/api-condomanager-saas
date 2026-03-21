package br.com.doistech.apicondomanagersaas.service.storage;

import org.springframework.stereotype.Service;

@Service
public class StoragePathService {

    public String condominioModuleFolder(Long condominioId, String module) {
        if (condominioId == null) {
            throw new IllegalArgumentException("CondominioId é obrigatório.");
        }
        if (module == null || module.isBlank()) {
            throw new IllegalArgumentException("Módulo do storage é obrigatório.");
        }
        return "condominios/" + condominioId + "/" + module;
    }
}
