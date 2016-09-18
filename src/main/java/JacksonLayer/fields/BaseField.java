package JacksonLayer.fields;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static JacksonLayer.utils.utils.isNotEmpty;

/**
 * Created by bresai on 16/9/6.
 */
public class BaseField<T> extends Field<T> {
    protected BaseSerializer<T> serializer;

    protected BaseDeserializer<T> deserializer;

    protected String serializerMethod;

    protected String deserializerMethod;

    protected Object serializerInstance;

    public BaseField(Builder builder) {
        super(builder);
        this.serializerMethod = builder.serializerMethod;
        this.deserializerMethod = builder.deserializerMethod;

    }

    public void setSerializer(BaseSerializer<T> serializer) {
        if (isNotEmpty(this.serializerMethod)) {
            this.serializer = new CustomizedSerializer(this);
        }else{
            this.serializer = serializer;
        }
    }

    public void setDeserializer(BaseDeserializer<T> deserializer) {
        if (isNotEmpty(this.deserializerMethod)) {
            this.deserializer = new CustomizedDeserializer(this);
        }else{
            this.deserializer = deserializer;
        }
    }

    public BaseSerializer<T> getSerializer() {
        return serializer;
    }

    public BaseDeserializer<T> getDeserializer() {
        return deserializer;
    }

    public String getSerializerMethod() {
        return serializerMethod;
    }

    public String getDeserializerMethod() {
        return deserializerMethod;
    }

    public Object getSerializerInstance() {
        return serializerInstance;
    }

    public void setSerializerInstance(Object serializerInstance) {
        this.serializerInstance = serializerInstance;
    }

    public abstract class BaseSerializer<T> extends JsonSerializer<T> {
        protected BaseField<T> field;

        public BaseSerializer(BaseField<T> field){
            super();
            this.field = field;
        }
    }

    public abstract class BaseDeserializer<T> extends JsonDeserializer<T> {
        protected BaseField<T> field;

        public BaseDeserializer(BaseField<T> field){
            super();
            this.field = field;
        }
    }

    public class CustomizedSerializer extends BaseSerializer<T>{

        public CustomizedSerializer(BaseField<T> field) {
            super(field);
        }

        @Override
        public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            Object instance = field.getSerializerInstance();
            try {
                Method method = instance.getClass().getMethod(field.getSerializerMethod(), Object.class);
                Object result = method.invoke(instance, value);
                gen.writeObject(result);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("customized method not found");
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public class CustomizedDeserializer extends BaseDeserializer<T>{

        public CustomizedDeserializer(BaseField<T> field) {
            super(field);
        }


        @Override
        @SuppressWarnings("unchecked")
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            Object result;

            Object value = new UntypedObjectDeserializer().deserialize(p, ctxt);

            Object instance = field.getSerializerInstance();
            try {
                Method method = instance.getClass().getMethod(field.getDeserializerMethod(), Object.class);
                result = method.invoke(instance, value);
                return (T) result;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("customized method not found");
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("the defined customized method is not accessible, please define it as public");
            } catch (ClassCastException e){
                throw new RuntimeException("the return type of the customized method is not correct");
            }
        }
    }

    public static class Builder<B extends Builder> extends Field.Builder<B>{
        protected String serializerMethod = null;
        protected String deserializerMethod = null;

        public B setSerMethod(String name) {
            this.serializerMethod = name;
            return (B) this;
        }

        public B setDeserMethod(String name) {
            this.deserializerMethod = name;
            return (B) this;
        }

        @Override
        public BaseField build() {
            return new BaseField(this);
        }
    }
}
