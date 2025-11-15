package br.com.felipebrandao.menufacil.service.impl;

import br.com.felipebrandao.menufacil.model.UnitType;
import br.com.felipebrandao.menufacil.repository.UnitTypeRepository;
import br.com.felipebrandao.menufacil.service.UnitTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnitTypeServiceImpl implements UnitTypeService {

    private final UnitTypeRepository repository;

    @Override
    public List<UnitType> list() {
        return repository.findAll();
    }

    @Override
    public UnitType create(UnitType unit) {
        if (repository.existsByName(unit.getName())) {
            throw new IllegalArgumentException("Nome da unidade já existe");
        }
        try {
            return repository.save(unit);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("Nome da unidade já existe");
        }
    }

    @Override
    public Optional<UnitType> update(String id, UnitType unit) {
        return repository.findById(id)
                .map(existing -> {
                    repository.findByName(unit.getName())
                            .ifPresent(conflict -> {
                                if (!conflict.getId().equals(id)) {
                                    throw new IllegalArgumentException("Nome da unidade já existe");
                                }
                            });
                    unit.setId(id);
                    try {
                        return repository.save(unit);
                    } catch (DuplicateKeyException e) {
                        throw new IllegalArgumentException("Nome da unidade já existe");
                    }
                });
    }

    @Override
    public boolean delete(String id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
