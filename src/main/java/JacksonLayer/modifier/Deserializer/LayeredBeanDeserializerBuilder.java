package JacksonLayer.modifier.Deserializer;

import JacksonLayer.fields.BaseField;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdValueProperty;

import java.util.Collection;
import java.util.Map;

/**
 * Created by bresai on 16/9/13.
 */
public class LayeredBeanDeserializerBuilder extends BeanDeserializerBuilder {
    private Map<String, BaseField> fields;

    public LayeredBeanDeserializerBuilder(BeanDescription beanDesc, DeserializationConfig config, Map<String, BaseField> fields) {
        super(beanDesc, config);
        this.fields = fields;
    }

    protected LayeredBeanDeserializerBuilder(BeanDeserializerBuilder src) {
        super(src);
    }

    @Override
    public JsonDeserializer<?> build()
    {
        for(Map.Entry<String, BaseField> field : fields.entrySet()){
            if (_properties.containsKey(field.getKey())){
                SettableBeanProperty property = _properties.get(field.getKey());
                _properties.remove(field.getKey());
//                SettableBeanProperty newProperty = new SettableBeanProperty(property, field.getValue().getLabel());
                _properties.put(field.getValue().getLabel(), property);
            }
        }
        Collection<SettableBeanProperty> props = _properties.values();
        BeanPropertyMap propertyMap = BeanPropertyMap.construct(props, _caseInsensitivePropertyComparison);
        propertyMap.assignIndexes();

        // view processing must be enabled if:
        // (a) fields are not included by default (when deserializing with view), OR
        // (b) one of properties has view(s) to included in defined
        boolean anyViews = !_defaultViewInclusion;

        if (!anyViews) {
            for (SettableBeanProperty prop : props) {
                if (prop.hasViews()) {
                    anyViews = true;
                    break;
                }
            }
        }

        // one more thing: may need to create virtual ObjectId property:
        if (_objectIdReader != null) {
            /* 18-Nov-2012, tatu: May or may not have annotations for id property;
             *   but no easy access. But hard to see id property being optional,
             *   so let's consider required at this point.
             */
            ObjectIdValueProperty prop = new ObjectIdValueProperty(_objectIdReader, PropertyMetadata.STD_REQUIRED);
            propertyMap = propertyMap.withProperty(prop);
        }


        return new BeanDeserializer(this,
                _beanDesc, propertyMap, _backRefProperties, _ignorableProps, _ignoreAllUnknown,
                anyViews);
    }
}
