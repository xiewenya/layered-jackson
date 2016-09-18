package JacksonLayer.fields;

/**
 * Created by bresai on 16/9/5.
 */
public abstract class Field<T> {
    protected Boolean readOnly = false;

    protected Boolean writeOnly = false;

    protected Boolean required = false;

    protected T defaultValue = null;

    protected String label;

    @SuppressWarnings("unchecked")
    public Field(Builder builder) {
        this.readOnly = builder.readOnly;
        this.writeOnly = builder.writeOnly;
        this.required = builder.required;
        this.defaultValue = (T) builder.defaultValue;
        this.label = builder.label;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public Boolean getWriteOnly() {
        return writeOnly;
    }

    public Boolean getRequired() {
        return required;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public String getLabel() {
        return label;
    }

    public Builder getBuilder(){
        return new Builder();
    }

    public static class Builder<B extends Builder>{
        protected Boolean readOnly = false;
        protected Boolean writeOnly = false;
        protected Boolean required = false;
        protected Object defaultValue = null;
        protected String label = null;

        public Builder() {
        }


        public B setReadOnly(Boolean readOnly) {
            this.readOnly = readOnly;
            return (B) this;
        }

        public B setWriteOnly(Boolean writeOnly) {
            this.writeOnly = writeOnly;
            return (B) this;
        }

        public B setRequired(Boolean required) {
            this.required = required;
            return (B) this;
        }

        public B setDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return (B) this;
        }

        public B setLabel(String label) {
            this.label = label;
            return (B) this;
        }

        public Field build() {
            return null;
        }
    }
}
