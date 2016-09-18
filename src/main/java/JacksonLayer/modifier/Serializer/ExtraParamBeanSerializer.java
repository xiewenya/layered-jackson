package JacksonLayer.modifier.Serializer;

import JacksonLayer.fields.ExtraField;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.impl.BeanAsArraySerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.NameTransformer;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static JacksonLayer.utils.utils.isNotEmpty;

/**
 * Created by bresai on 16/9/7.
 */
public class ExtraParamBeanSerializer extends BeanSerializerBase {

    private Map<String, ExtraField> extraFields;

    public ExtraParamBeanSerializer(JavaType type, BeanSerializerBuilder builder, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties, Map<String, ExtraField> extraFields) {
        super(type, builder, properties, filteredProperties);
        this.extraFields = extraFields;
    }

    protected ExtraParamBeanSerializer(BeanSerializerBase src, Set<String> toIgnore) {
        super(src, toIgnore);
    }

    public ExtraParamBeanSerializer(BeanSerializerBase src, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        super(src, properties, filteredProperties);
    }

    public ExtraParamBeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter) {
        super(src, objectIdWriter);
    }

    public ExtraParamBeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter, Object filterId) {
        super(src, objectIdWriter, filterId);
    }

    public ExtraParamBeanSerializer(BeanSerializerBase src, String[] toIgnore) {
        super(src, toIgnore);
    }

    public ExtraParamBeanSerializer(BeanSerializerBase src) {
        super(src);
    }

    public ExtraParamBeanSerializer(BeanSerializerBase src, NameTransformer unwrapper) {
        super(src, unwrapper);
    }

    public ExtraParamBeanSerializer(BeanSerializerBase serializer, Map<String, ExtraField> extraFields) {
        super(serializer);
        this.extraFields = extraFields;
    }

    @Override
    public JsonSerializer<Object> unwrappingSerializer(NameTransformer unwrapper) {
        return new UnwrappingBeanSerializer(this, unwrapper);
    }

    @Override
    public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
        return new ExtraParamBeanSerializer(this, objectIdWriter, _propertyFilterId);
    }

    @Override
    protected BeanSerializerBase withIgnorals(Set<String> toIgnore) {
        return new ExtraParamBeanSerializer(this, toIgnore);
    }

    @Override
    public BeanSerializerBase withFilterId(Object filterId) {
        return new ExtraParamBeanSerializer(this, _objectIdWriter, filterId);
    }

    @Override
    protected BeanSerializerBase asArraySerializer()
    {
        /* Can not:
         *
         * - have Object Id (may be allowed in future)
         * - have "any getter"
         * - have per-property filters
         */
        if ((_objectIdWriter == null)
                && (_anyGetterWriter == null)
                && (_propertyFilterId == null)
                ) {
            return new BeanAsArraySerializer(this);
        }
        // already is one, so:
        return this;
    }

    @Override
    public void serialize(Object bean, JsonGenerator gen, SerializerProvider provider)
            throws IOException
    {
        if (_objectIdWriter != null) {
            gen.setCurrentValue(bean); // [databind#631]
            _serializeWithObjectId(bean, gen, provider, true);
            return;
        }
        gen.writeStartObject();
        // [databind#631]: Assign current value, to be accessible by custom serializers
        gen.setCurrentValue(bean);
        if (_propertyFilterId != null) {
            serializeFieldsFiltered(bean, gen, provider);
        } else {
            serializeFields(bean, gen, provider);
        }

        if (isNotEmpty(extraFields)){
            serializeExtraFields(bean, extraFields, gen, provider);
        }


        gen.writeEndObject();
    }

//    private void manageProperties(Map<String, ExtraField> extraFields){
//        for (Map.Entry<String, ExtraField> entry : extraFields.entrySet()){
//            if (! serializer.getIncludedProps().contains(writer.getName())){
//                beanProperties.remove(writer);
//            }
//
//            if (serializer.getIgnoredProps().contains(writer.getName())){
//                beanProperties.remove(writer);
//            }
//        }
//    }

    private void serializeExtraFields(Object bean, Map<String, ExtraField> extraFields, JsonGenerator gen, SerializerProvider provider) throws IOException {
        for (Map.Entry<String, ExtraField> entry : extraFields.entrySet()){
            gen.writeFieldName(entry.getKey());
            entry.getValue().getSerializer().serialize(bean, gen, provider);
        }
    }
}
