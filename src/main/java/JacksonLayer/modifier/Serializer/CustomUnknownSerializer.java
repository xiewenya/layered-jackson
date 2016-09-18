package JacksonLayer.modifier.Serializer;

import JacksonLayer.fields.ExtraField;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.UnknownSerializer;

import java.io.IOException;
import java.util.Map;

import static JacksonLayer.utils.utils.isNotEmpty;

/**
 * Created by bresai on 16/9/14.
 */
public class CustomUnknownSerializer extends UnknownSerializer {
    private Map<String, ExtraField> extraFields;

    public CustomUnknownSerializer(Map<String, ExtraField> extraFields) {
        super(Object.class);
        this.extraFields = extraFields;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException
    {

        // But if it's fine, we'll just output empty JSON Object:
        gen.writeStartObject();

        if (isNotEmpty(extraFields)){
            serializeExtraFields(value, extraFields, gen, provider);
        }

        gen.writeEndObject();
    }

    private void serializeExtraFields(Object bean, Map<String, ExtraField> extraFields, JsonGenerator gen, SerializerProvider provider) throws IOException {
        for (Map.Entry<String, ExtraField> entry : extraFields.entrySet()){
            gen.writeFieldName(entry.getKey());
            entry.getValue().getSerializer().serialize(bean, gen, provider);
        }
    }
}
