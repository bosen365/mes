package com.qcadoo.mes.orders.hooks;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.OrderPackService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderPackFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderPackDetailsHooks {

    @Autowired
    private NumberService numberService;

    @Autowired
    private OrderPackService orderPackService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OrderPackFields.ORDER);
        Entity order = orderLookup.getEntity();

        if (order != null) {
            FieldComponent orderQuantity = (FieldComponent) view.getComponentByReference("orderQuantity");
            orderQuantity.setFieldValue(numberService.format(order.getField(OrderFields.PLANNED_QUANTITY)));
            FieldComponent quantityField = (FieldComponent) view.getComponentByReference(OrderPackFields.QUANTITY);
            FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
            BigDecimal sumQuantityOrderPacks = orderPackService.getSumQuantityOrderPacksForOrderWithoutPack(order,
                    form.getEntityId());
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils
                    .tryParseAndIgnoreSeparator((String) quantityField.getFieldValue(), LocaleContextHolder.getLocale());
            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                sumQuantityOrderPacks = sumQuantityOrderPacks.add(eitherNumber.getRight().get(), numberService.getMathContext());
            }
            FieldComponent sumQuantityOrderPacksField = (FieldComponent) view.getComponentByReference("sumQuantityOrderPacks");
            sumQuantityOrderPacksField.setFieldValue(numberService.format(sumQuantityOrderPacks));
            String unit = order.getBelongsToField(OrderFields.PRODUCT).getStringField(ProductFields.UNIT);
            FieldComponent orderQuantityUnit = (FieldComponent) view.getComponentByReference("orderQuantityUnit");
            orderQuantityUnit.setFieldValue(unit);
            orderQuantityUnit.requestComponentUpdateState();
            FieldComponent sumQuantityOrderPacksUnit = (FieldComponent) view.getComponentByReference("sumQuantityOrderPacksUnit");
            sumQuantityOrderPacksUnit.setFieldValue(unit);
            sumQuantityOrderPacksUnit.requestComponentUpdateState();
            FieldComponent quantityUnit = (FieldComponent) view.getComponentByReference("quantityUnit");
            quantityUnit.setFieldValue(unit);
            quantityUnit.requestComponentUpdateState();
        }

    }
}
