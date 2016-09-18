package JacksonLayer.modifier;

import JacksonLayer.modifier.Serializer.LayeredBeanPropertyWriter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyBuilder;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.NameTransformer;

/**
 * Created by bresai on 16/9/5.
 */
public class LayeredPropertyWriterBuilder extends PropertyBuilder {
    public LayeredPropertyWriterBuilder(SerializationConfig config, BeanDescription beanDesc) {
        super(config, beanDesc);
    }

    /**
     * @param contentTypeSer Optional explicit type information serializer
     *    to use for contained values (only used for properties that are
     *    of container type)
     */
    protected BeanPropertyWriter buildWriter(SerializerProvider prov,
                                             BeanPropertyDefinition propDef, JavaType declaredType, JsonSerializer<?> ser,
                                             TypeSerializer typeSer, TypeSerializer contentTypeSer,
                                             AnnotatedMember am, boolean defaultUseStaticTyping)
            throws JsonMappingException
    {
        // do we have annotation that forces type to use (to declared type or its super type)?
        JavaType serializationType = findSerializationType(am, defaultUseStaticTyping, declaredType);

        // Container types can have separate type serializers for content (value / element) type
        if (contentTypeSer != null) {
            /* 04-Feb-2010, tatu: Let's force static typing for collection, if there is
             *    type information for contents. Should work well (for JAXB case); can be
             *    revisited if this causes problems.
             */
            if (serializationType == null) {
//                serializationType = TypeFactory.type(am.getGenericType(), _beanDesc.getType());
                serializationType = declaredType;
            }
            JavaType ct = serializationType.getContentType();
            // Not exactly sure why, but this used to occur; better check explicitly:
            if (ct == null) {
                throw new IllegalStateException("Problem trying to create BeanPropertyWriter for property '"
                        +propDef.getName()+"' (of type "+_beanDesc.getType()+"); serialization type "+serializationType+" has no content");
            }
            serializationType = serializationType.withContentTypeHandler(contentTypeSer);
            ct = serializationType.getContentType();
        }

        Object valueToSuppress = null;
        boolean suppressNulls = false;

        // 12-Jul-2016, tatu: [databind#1256] Need to make sure we consider type refinement
        JavaType actualType = (serializationType == null) ? declaredType : serializationType;

        // 17-Aug-2016, tatu: Default inclusion covers global default (for all types), as well
        //   as type-default for enclosing POJO. What we need, then, is per-type default (if any)
        //   for declared property type... and finally property annotation overrides
        JsonInclude.Value inclV = _config.getDefaultPropertyInclusion(actualType.getRawClass(),
                _defaultInclusion);

        // property annotation override

        inclV = inclV.withOverrides(propDef.findInclusion());
        JsonInclude.Include inclusion = inclV.getValueInclusion();

        if (inclusion == JsonInclude.Include.USE_DEFAULTS) { // should not occur but...
            inclusion = JsonInclude.Include.ALWAYS;
        }

        switch (inclusion) {
            case NON_DEFAULT:
                // 11-Nov-2015, tatu: This is tricky because semantics differ between cases,
                //    so that if enclosing class has this, we may need to values of property,
                //    whereas for global defaults OR per-property overrides, we have more
                //    static definition. Sigh.
                // First: case of class specifying it; try to find POJO property defaults
                if (_defaultInclusion.getValueInclusion() == JsonInclude.Include.NON_DEFAULT) {
                    valueToSuppress = getPropertyDefaultValue(propDef.getName(), am, actualType);
                } else {
                    valueToSuppress = getDefaultValue(actualType);
                }
                if (valueToSuppress == null) {
                    suppressNulls = true;
                } else {
                    if (valueToSuppress.getClass().isArray()) {
                        valueToSuppress = ArrayBuilders.getArrayComparator(valueToSuppress);
                    }
                }

                break;
            case NON_ABSENT: // new with 2.6, to support Guava/JDK8 Optionals
                // always suppress nulls
                suppressNulls = true;
                // and for referential types, also "empty", which in their case means "absent"
                if (actualType.isReferenceType()) {
                    valueToSuppress = BeanPropertyWriter.MARKER_FOR_EMPTY;
                }
                break;
            case NON_EMPTY:
                // always suppress nulls
                suppressNulls = true;
                // but possibly also 'empty' values:
                valueToSuppress = BeanPropertyWriter.MARKER_FOR_EMPTY;
                break;
            case NON_NULL:
                suppressNulls = true;
                // fall through
            case ALWAYS: // default
            default:
                // we may still want to suppress empty collections, as per [JACKSON-254]:
                if (actualType.isContainerType()
                        && !_config.isEnabled(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)) {
                    valueToSuppress = BeanPropertyWriter.MARKER_FOR_EMPTY;
                }
                break;
        }

        BeanPropertyWriter bpw = new LayeredBeanPropertyWriter(propDef,
                am, _beanDesc.getClassAnnotations(), declaredType,
                ser, typeSer, serializationType, suppressNulls, valueToSuppress);

        // How about custom null serializer?
        Object serDef = _annotationIntrospector.findNullSerializer(am);
        if (serDef != null) {
            bpw.assignNullSerializer(prov.serializerInstance(am, serDef));
        }
        // And then, handling of unwrapping
        NameTransformer unwrapper = _annotationIntrospector.findUnwrappingNameTransformer(am);
        if (unwrapper != null) {
            bpw = bpw.unwrappingWriter(unwrapper);
        }
        return bpw;
    }
}