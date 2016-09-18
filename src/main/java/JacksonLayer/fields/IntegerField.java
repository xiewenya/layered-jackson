package JacksonLayer.fields;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Created by bresai on 16/8/30.
 */
public class IntegerField extends BaseField<Integer> {

    public IntegerField(Builder builder) {
        super(builder);
        this.setSerializer(new IntegerSerializer(this));
        this.setDeserializer(new IntegerDeserializer(this));
    }

    public class IntegerSerializer extends BaseSerializer<Integer>{

        public IntegerSerializer(BaseField<Integer> field) {
            super(field);
        }

        @Override
        public void serialize(Integer value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            if (value == null){
                gen.writeNull();
            }else{
                gen.writeNumber(value);
            }
        }
    }

    public class IntegerDeserializer extends BaseDeserializer<Integer>{

        public IntegerDeserializer(BaseField<Integer> field) {
            super(field);
        }

        @Override
        public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return null;
        }
    }

    public static final class Builder extends BaseField.Builder<Builder>{
        @Override
        public IntegerField build() {
            return new IntegerField(this);
        }
    }

}
