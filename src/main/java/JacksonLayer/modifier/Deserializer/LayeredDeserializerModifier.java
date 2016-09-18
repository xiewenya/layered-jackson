package JacksonLayer.modifier.Deserializer;

import JacksonLayer.fields.BaseField;
import JacksonLayer.modifier.LayeredSerializer;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.POJOPropertyBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static JacksonLayer.utils.utils.isEmpty;
import static JacksonLayer.utils.utils.isNotEmpty;

/**
 * Created by bresai on 16/9/12.
 */
public class LayeredDeserializerModifier extends BeanDeserializerModifier{
    private LayeredSerializer serializer;

    private Map<String, BaseField> fields;

    private List<String> ignored;

    public LayeredDeserializerModifier(LayeredSerializer serializer) {
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
                baseFields.put(field.getName(), (BaseField) field.get(serializer));
            }
        }
        return baseFields;
    }

    private void manageProperties(List<BeanPropertyDefinition> propDefs, LayeredSerializer serializer) {

        Iterator<BeanPropertyDefinition> iterator = propDefs.iterator();
        while (iterator.hasNext()) {
            BeanPropertyDefinition property = iterator.next();
            if (!serializer.getIncludedProps().contains(property.getName()) && isNotEmpty(serializer.getIncludedProps())) {
                iterator.remove();
            }

            if (serializer.getIgnoredProps().contains(property.getName())) {
                iterator.remove();
            }
        }
    }

    @Override
    public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
                                                         BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
        if (isEmpty(fields)) {
            manageProperties(propDefs, serializer);
            return propDefs;
        }

        for( int i = 0; i < propDefs.size(); i++){
            BeanPropertyDefinition property = propDefs.get(i);
            if (fields.containsKey(property.getName())){
                BaseField field = fields.get(property.getName());
                if (field.getReadOnly()){
                    addIgnored(property.getName());
                    propDefs.remove(i);
                    continue;
                }
                String newName = field.getLabel();
                if (newName != null){
                    BeanPropertyDefinition newDefinition = new POJOPropertyBuilder((POJOPropertyBuilder) property, new PropertyName(newName));
                    propDefs.set(i, newDefinition);
                }


            }
        }

        manageProperties(propDefs, serializer);

        return propDefs;
    }

    private void addIgnored(String name){
        if (ignored == null){
            ignored = new ArrayList<String>();
        }
        ignored.add(name);
    }

    @Override
    public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
                                                 BeanDescription beanDesc, BeanDeserializerBuilder builder) {
        if (isEmpty(fields)) {
            return builder;
        }

        if (isNotEmpty(ignored)){
            for(String name : ignored){
                builder.addIgnorable(name);
            }
        }

        Iterator<SettableBeanProperty> iterator = builder.getProperties();

        while ( iterator.hasNext() ){
            SettableBeanProperty property = iterator.next();
            Constructor constructor = null;
            try {
                constructor = property.getClass().getDeclaredConstructor(property.getClass(), JsonDeserializer.class);
                constructor.setAccessible(true);
                BaseField field = fields.get(property.getName());
                if (field != null){
                    Object instance = constructor.newInstance(property, field.getDeserializer());
                    builder.addOrReplaceProperty((SettableBeanProperty) instance, true);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return builder;
    }

}
