package br.com.felipebrandao.menufacil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDay {

    @Id
    private String id;

    private LocalDate date;

    @Builder.Default
    private List<ScheduledRecipe> recipes = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
