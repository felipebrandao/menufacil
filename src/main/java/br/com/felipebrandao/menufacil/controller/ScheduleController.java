package br.com.felipebrandao.menufacil.controller;

import br.com.felipebrandao.menufacil.dto.schedule.CreateScheduleRecipeRequest;
import br.com.felipebrandao.menufacil.dto.schedule.DayResponse;
import br.com.felipebrandao.menufacil.dto.schedule.ReorderRequest;
import br.com.felipebrandao.menufacil.dto.schedule.ScheduleListResponse;
import br.com.felipebrandao.menufacil.dto.schedule.ScheduledRecipeResponse;
import br.com.felipebrandao.menufacil.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<ScheduleListResponse> list(
            @RequestParam String view,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        ScheduleListResponse resp = scheduleService.list(view, start, year, month);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{date}")
    public ResponseEntity<DayResponse> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(scheduleService.getByDate(date));
    }

    @PostMapping("/{date}/recipes")
    public ResponseEntity<ScheduledRecipeResponse> addRecipe(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody CreateScheduleRecipeRequest req
    ) {
        ScheduledRecipeResponse r = scheduleService.addRecipe(date, req);
        return ResponseEntity.created(null).body(r);
    }

    @PatchMapping("/{date}/reorder")
    public ResponseEntity<Void> reorder(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody ReorderRequest req
    ) {
        scheduleService.reorder(date, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/scheduled/{scheduledId}")
    public ResponseEntity<Void> deleteScheduled(@PathVariable String scheduledId) {
        scheduleService.deleteScheduledRecipe(scheduledId);
        return ResponseEntity.noContent().build();
    }
}
