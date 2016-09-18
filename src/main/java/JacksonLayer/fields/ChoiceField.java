package JacksonLayer.fields;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

/**
 * Created by bresai on 16/8/30.
 */
public class ChoiceField extends BaseField<Object> {

    private Map<?, String> choices;

    public Map<?, String> getChoices() {
        return choices;
    }

    public ChoiceField(Builder builder) {
        super(builder);
        this.choices = builder.choices;
        this.setSerializer(new ChoiceSerializer(this));
        this.setDeserializer(new ChoiceDeserializer(this));
    }

    public class ChoiceSerializer extends BaseSerializer<Object>{

        public ChoiceSerializer(ChoiceField field) {
            super(field);
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            Map<?, String> choices = ((ChoiceField) field).getChoices();
            if (choices.containsKey(value)){
                gen.writeString(choices.get(value));
            }
            else{
                throw new RuntimeException("can not serializer the given value");
            }
        }
    }

    public class ChoiceDeserializer extends BaseDeserializer<Object>{

        public ChoiceDeserializer(ChoiceField field) {
            super(field);
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonToken token = p.getCurrentToken();
            Object result = null;
            if (token == JsonToken.VALUE_STRING){
                String value = p.getText();
                for (Map.Entry<?, String> entry : choices.entrySet()){
                    if (entry.getValue().equals(value)){
                        result = entry.getKey();
                        break;
                    }
                }
            }
            return result;
        }
    }

    public static final class Builder extends BaseField.Builder<Builder>{
        private Map<?, String> choices;

        public Builder setChoices(Map<?, String> choices){
            this.choices = choices;
            return this;
        }
        @Override
        public ChoiceField build() {
            return new ChoiceField(this);
        }
    }
}
