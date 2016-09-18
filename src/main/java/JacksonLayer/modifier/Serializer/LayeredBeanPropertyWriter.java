package JacksonLayer.modifier.Serializer;

import JacksonLayer.fields.BaseField;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.util.Annotations;

/**
 * Created by bresai on 16/9/5.
 */
public class LayeredBeanPropertyWriter extends BeanPropertyWriter {
    private BaseField baseField;

    public LayeredBeanPropertyWriter(BeanPropertyDefinition propDef, AnnotatedMember member, Annotations contextAnnotations, JavaType declaredType, JsonSerializer<?> ser, TypeSerializer typeSer, JavaType serType, boolean suppressNulls, Object suppressableValue) {
        super(propDef, member, contextAnnotations, declaredType, ser, typeSer, serType, suppressNulls, suppressableValue);
    }

    public void setBaseField(BaseField baseField) {
        this.baseField = baseField;
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception
    {
        if (baseField == null) {
            super.serializeAsField(bean, gen, prov);
            return;
        }

        if (baseField.getWriteOnly()){
            return;
        }

        // inlined 'get()'
        final Object value = (_accessorMethod == null) ? _field.get(bean) : _accessorMethod.invoke(bean);


        SerializedString defaultName;
        if (baseField != null && baseField.getLabel() != null){
            defaultName = new SerializedString(baseField.getLabel());
        }
        else{
            defaultName = _name;
        }
        // Null handling is bit different, check that first
        if (value == null) {
            if (_nullSerializer != null) {
                gen.writeFieldName(defaultName);
                _nullSerializer.serialize(
                        baseField.getDefaultValue()==null? null: baseField.getDefaultValue(),
                        gen, prov);
            }
            return;
        }
        // then find serializer to use
        JsonSerializer<Object> ser = _serializer;
        if (ser == null) {
            Class<?> cls = value.getClass();
            PropertySerializerMap m = _dynamicSerializers;
            ser = m.serializerFor(cls);
            if (ser == null) {
                ser = _findAndAddDynamic(m, cls, prov);
            }
        }
        // and then see if we must suppress certain values (default, empty)
        if (_suppressableValue != null) {
            if (MARKER_FOR_EMPTY == _suppressableValue) {
                if (ser.isEmpty(prov, value)) {
                    return;
                }
            } else if (_suppressableValue.equals(value)) {
                return;
            }
        }
        // For non-nulls: simple check for direct cycles
        if (value == bean) {
            // three choices: exception; handled by call; or pass-through
            if (_handleSelfReference(bean, gen, prov, ser)) {
                return;
            }
        }

//        if (_serializer instanceof BaseField.BaseSerializer){
////            ((BaseSerializer) _serializer).getDefaultValue()
//        }
        gen.writeFieldName(defaultName);
        if (_typeSerializer == null) {
            ser.serialize(value, gen, prov);
        } else {
            ser.serializeWithType(value, gen, prov, _typeSerializer);
        }
    }
}
