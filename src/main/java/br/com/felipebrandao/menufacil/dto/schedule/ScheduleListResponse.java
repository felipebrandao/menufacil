package br.com.felipebrandao.menufacil.dto.schedule;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ScheduleListResponse {
    private String view;
    private LocalDate start;
    private LocalDate end;
    private List<DayResponse> days;
}
