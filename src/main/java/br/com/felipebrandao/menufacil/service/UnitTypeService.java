package br.com.felipebrandao.menufacil.service;

import br.com.felipebrandao.menufacil.model.UnitType;

import java.util.List;
import java.util.Optional;

public interface UnitTypeService {
    List<UnitType> list();
    UnitType create(UnitType unit);
    Optional<UnitType> update(String id, UnitType unit);
    boolean delete(String id);
}
