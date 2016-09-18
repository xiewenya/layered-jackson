package JacksonLayer.fields;

import static JacksonLayer.utils.utils.isEmpty;

/**
 * Created by bresai on 16/9/6.
 */
public class ExtraField extends BaseField<Object>{

    public ExtraField(Builder builder) {
        super(builder);
        this.readOnly = true;
        this.writeOnly = false;
        this.required = false;
        this.deserializerMethod = null;
        this.setSerializer(new CustomizedSerializer(this));
        this.setDeserializer(null);

        if (isEmpty(this.serializerMethod)){
            throw new RuntimeException("Serializer method is mandatory in extraField");
        }
    }

    public Object getSerializerInstance() {
        return serializerInstance;
    }

    public void setSerializerInstance(Object serializerInstance) {
        this.serializerInstance = serializerInstance;
    }

    public static final class Builder extends BaseField.Builder<Builder>{


        @Override
        public Builder setReadOnly(Boolean readOnly){
            if (readOnly){
                this.readOnly = true;
            }
            else{
                throw new RuntimeException("Extra field is always read only, not allowed to set to false");
            }

            return this;
        }

        @Override
        public Builder setWriteOnly(Boolean writeOnly) {
            throw new RuntimeException("Extra field is always read only, not allowed to use setWriteOnly");
        }

        @Override
        public Builder setRequired(Boolean required) {
            throw new RuntimeException("Extra field is always read only, not allowed to use setRequired");
        }

        @Override
        public ExtraField build() {
            return new ExtraField(this);
        }
    }
}
