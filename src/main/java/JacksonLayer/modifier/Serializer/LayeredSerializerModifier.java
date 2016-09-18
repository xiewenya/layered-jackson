package JacksonLayer.modifier.Serializer;

import JacksonLayer.fields.BaseField;
import JacksonLayer.fields.ExtraField;
import JacksonLayer.modifier.LayeredSerializer;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.impl.UnknownSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static JacksonLayer.utils.utils.isNotEmpty;

/**
 * Created by bresai on 16/9/5.
 */
public class LayeredSerializerModifier extends BeanSerializerModifier {
    private LayeredSerializer serializer;

    private Map<String, ExtraField> extraFields;
    private Map<String, BaseField> fields;

    public LayeredSerializerModifier(LayeredSerializer serializer) {
        this.serializer = serializer;

        try {
            this.fields = collectFields(serializer);
        } catch (IllegalAccessException e) {
            this.fields = null;
        }
    }

    private Map<String, BaseField> collectFields(LayeredSerializer serializer) throws IllegalAccessException {
        Map<String, BaseField> baseFields = new HashMap<String, BaseField>();
        Field[] fields = serializer.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);

            //if the field is defined but not set in our serializer than just use normal jackson serializer
            if (field.getType().isAssignableFrom(BaseField.class) && field.get(serializer) != null) {
                BaseField baseField = (BaseField) field.get(serializer);
                baseField.setSerializerInstance(serializer);
                baseFields.put(field.getName(), (BaseField) field.get(serializer));
            }
        }
        return baseFields;
    }

    private void manageProperties(List<BeanPropertyWriter> beanProperties, LayeredSerializer serializer) {

        Iterator<BeanPropertyWriter> iterator = beanProperties.iterator();
        while (iterator.hasNext()) {
            BeanPropertyWriter writer = iterator.next();
            if (!serializer.getIncludedProps().contains(writer.getName()) && isNotEmpty(serializer.getIncludedProps())) {
                iterator.remove();
            }

            if (serializer.getIgnoredProps().contains(writer.getName())) {
                iterator.remove();
            }
        }

        if (extraFields != null){
            for (Map.Entry<String, ExtraField> entry : extraFields.entrySet()){
                if (!serializer.getIncludedProps().contains(entry.getKey()) && isNotEmpty(serializer.getIncludedProps())) {
                    extraFields.remove(entry.getKey());
                }

                if (serializer.getIgnoredProps().contains(entry.getKey())) {
                    extraFields.remove(entry.getKey());
                }
            }
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        Map<String, BaseField> extras = fields;

        if (fields == null) {
            manageProperties(beanProperties, serializer);
            return beanProperties;
        }


        for (BeanPropertyWriter writer : beanProperties) {
            if (fields.containsKey(writer.getName())) {
                BaseField field = fields.get(writer.getName());
                ((LayeredBeanPropertyWriter) writer).setBaseField(field);

                /*
                set the null serializer to serializer in order to avoid setting nullSerializer
                by the provider later
                 */
                writer.assignNullSerializer(fields.get(writer.getName()).getSerializer());

                writer.assignSerializer(fields.get(writer.getName()).getSerializer());

                extras.remove(writer.getName());
            }
        }

        setExtraFields(extras);

        //here we just check properties defined in bean, the extra properties need to be checked again
        manageProperties(beanProperties, serializer);

        return beanProperties;
    }

    private void setExtraFields(Map<String, BaseField> extras) {
        if (!extras.isEmpty()) {
            if (extraFields == null){
                extraFields = new HashMap<String, ExtraField>();
            }

            for (Map.Entry<String, BaseField> entry : extras.entrySet()) {
                if (entry.getValue() instanceof ExtraField) {
                    extraFields.put(entry.getKey(), (ExtraField) entry.getValue());
                }
            }
        }
    }

//    @Override
//    @SuppressWarnings("unchecked")
//    public BeanSerializerBuilder updateBuilder(SerializationConfig config,
//                                               BeanDescription beanDesc,
//                                               BeanSerializerBuilder builder) {
//        return new LayeredBeanSerializerBuilder(builder, extraFields);
//    }

    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config,
                                              BeanDescription beanDesc,
                                              JsonSerializer<?> serializer) {
        if (serializer instanceof BeanSerializer && isNotEmpty(extraFields)){
            return new ExtraParamBeanSerializer((BeanSerializerBase) serializer, extraFields);
        }

        if (serializer instanceof UnknownSerializer && isNotEmpty(extraFields)){
            return new CustomUnknownSerializer(extraFields);
        }
        return serializer;
    }


}
