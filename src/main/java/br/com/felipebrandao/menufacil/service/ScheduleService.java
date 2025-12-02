package br.com.felipebrandao.menufacil.service;

import br.com.felipebrandao.menufacil.dto.schedule.CreateScheduleRecipeRequest;
import br.com.felipebrandao.menufacil.dto.schedule.DayResponse;
import br.com.felipebrandao.menufacil.dto.schedule.ReorderRequest;
import br.com.felipebrandao.menufacil.dto.schedule.ScheduleListResponse;
import br.com.felipebrandao.menufacil.dto.schedule.ScheduledRecipeResponse;

import java.time.LocalDate;

public interface ScheduleService {
    ScheduleListResponse list(String view, LocalDate start, Integer year, Integer month);
    DayResponse getByDate(LocalDate date);
    ScheduledRecipeResponse addRecipe(LocalDate date, CreateScheduleRecipeRequest req);
    void reorder(LocalDate date, ReorderRequest req);
    void deleteRecipe(LocalDate date, String scheduledId);
}
