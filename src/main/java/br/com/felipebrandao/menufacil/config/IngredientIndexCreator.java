package br.com.felipebrandao.menufacil.config;

import br.com.felipebrandao.menufacil.model.Ingredient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IngredientIndexCreator {

    private final MongoTemplate mongoTemplate;

    @EventListener(ContextRefreshedEvent.class)
    public void ensureIndexes() {
        TextIndexDefinition textIdx = new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField("name")
                .build();
        mongoTemplate.indexOps(Ingredient.class).createIndex(textIdx);

        Index uniqueNamePerCategory = new Index()
                .on("name", org.springframework.data.domain.Sort.Direction.ASC)
                .on("category", org.springframework.data.domain.Sort.Direction.ASC)
                .unique();
        mongoTemplate.indexOps(Ingredient.class).createIndex(uniqueNamePerCategory);
    }
}
