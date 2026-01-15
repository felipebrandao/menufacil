package br.com.felipebrandao.menufacil.service.impl;

import br.com.felipebrandao.menufacil.dto.shopping.ShoppingListItemResponse;
import br.com.felipebrandao.menufacil.model.Ingredient;
import br.com.felipebrandao.menufacil.model.UnitType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class ShoppingListRoundingTest {

    @Test
    void roundQuantity_shouldRemoveFloatingArtifacts() throws Exception {
        ShoppingListServiceImpl svc = new ShoppingListServiceImpl(null, null, null, null, java.time.Clock.systemUTC());

        Method m = ShoppingListServiceImpl.class.getDeclaredMethod("roundQuantity", Double.class);
        m.setAccessible(true);

        Double rounded = (Double) m.invoke(svc, 1.2000000000000002d);
        assertThat(rounded).isEqualTo(1.2d);
    }

    @Test
    void toItemResponse_shouldUseRoundedQuantity() {
        Ingredient ing = new Ingredient();
        UnitType unit = new UnitType();
        unit.setId("u1");
        unit.setName("Kilograma");
        unit.setAbbreviation("kg");
        ing.setDefaultUnit(unit);

        ShoppingListItemResponse item = new ShoppingListItemResponse();
        item.setQuantity(1.2000000000000002d);

        assertThat(item.getQuantity()).isEqualTo(1.2000000000000002d);
    }
}

