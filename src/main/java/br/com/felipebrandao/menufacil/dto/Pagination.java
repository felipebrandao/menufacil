package br.com.felipebrandao.menufacil.dto;

import lombok.Data;

@Data
public class Pagination {
    private int page;
    private int limit;
    private long total;
    private int totalPages;
}
