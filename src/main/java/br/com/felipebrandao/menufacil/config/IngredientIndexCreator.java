package br.com.felipebrandao.menufacil.config;

import br.com.felipebrandao.menufacil.model.Ingredient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IngredientIndexCreator {

    private final MongoTemplate mongoTemplate;

    @EventListener(ContextRefreshedEvent.class)
    public void ensureTextIndex() {
        TextIndexDefinition idx = new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField("name")
                .build();
        mongoTemplate.indexOps(Ingredient.class).createIndex(idx);
    }
}
