package JacksonLayer.modifier;

import JacksonLayer.modifier.Deserializer.LayeredBeanDeserializerFactory;
import JacksonLayer.modifier.Deserializer.LayeredDeserializerModifier;
import JacksonLayer.modifier.Serializer.LayeredBeanSerializerFactory;
import JacksonLayer.modifier.Serializer.LayeredSerializerModifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;

/**
 * Created by bresai on 16/9/1.
 */
public class LayeredObjectMapper extends ObjectMapper {

    private LayeredObjectMapper(DefaultDeserializationContext dc) {
        super(null, null, dc);
    }

    public static LayeredObjectMapper createInstance(LayeredSerializer serializer){
        DefaultDeserializationContext deserializationContext = new DefaultDeserializationContext.Impl(LayeredBeanDeserializerFactory.instance);
        LayeredObjectMapper mapper = new LayeredObjectMapper(deserializationContext);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializerFactory(new LayeredBeanSerializerFactory(null));
        mapper.registerModule(new LayeredModule(new LayeredSerializerModifier(serializer), new LayeredDeserializerModifier(serializer)));

        return mapper;
    }

}
