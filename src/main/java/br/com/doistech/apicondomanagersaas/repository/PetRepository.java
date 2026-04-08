package br.com.doistech.apicondomanagersaas.repository;

import br.com.doistech.apicondomanagersaas.domain.pet.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findAllByCondominioId(Long condominioId);
    List<Pet> findAllByCondominioIdAndUnidadeId(Long condominioId, Long unidadeId);
    List<Pet> findAllByCondominioIdAndUnidadeIdIn(Long condominioId, List<Long> unidadeIds);
    Optional<Pet> findByIdAndCondominioId(Long id, Long condominioId);
}
