package org.jbake.template;

import freemarker.template.*;
import org.jbake.model.DocumentModel;

import java.sql.Date;
import java.time.Instant;

public class FreemarkerJava8ObjectWrapper extends DefaultObjectWrapper {

    public FreemarkerJava8ObjectWrapper(Version incompatibleImprovements) {
        super(incompatibleImprovements);
    }

    @Override
    protected TemplateModel handleUnknownType(final Object obj) throws TemplateModelException {
        if (obj instanceof Instant) {
            return new InstantAdapter((Instant) obj, this);
        }

        return super.handleUnknownType(obj);
    }

    private static class InstantAdapter extends WrappingTemplateModel implements TemplateDateModel,
        AdapterTemplateModel {

        private final Instant instant;

        public InstantAdapter(Instant instant, FreemarkerJava8ObjectWrapper ow) {
            super(ow);  // coming from WrappingTemplateModel
            this.instant = instant;
        }

        @Override
        public Object getAdaptedObject(Class<?> hint) {
            return instant;
        }

        @Override
        public java.util.Date getAsDate() {
            return Date.from(instant);
        }

        @Override
        public int getDateType() {
            return TemplateDateModel.DATETIME;
        }
    }
}
