package br.com.felipebrandao.menufacil.dto.schedule;

import lombok.Data;

import java.util.List;

@Data
public class ReorderRequest {
    private List<String> order;
}
