package JacksonLayer.modifier.Serializer;

import JacksonLayer.modifier.LayeredPropertyWriterBuilder;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.PropertyBuilder;
import com.fasterxml.jackson.databind.ser.SerializerFactory;

/**
 * Created by bresai on 16/9/5.
 */
public class LayeredBeanSerializerFactory extends BeanSerializerFactory {
    /**
     * Constructor for creating instances with specified configuration.
     *
     * @param config
     */
    public LayeredBeanSerializerFactory(SerializerFactoryConfig config) {
        super(config);
    }

    protected PropertyBuilder constructPropertyBuilder(SerializationConfig config,
                                                       BeanDescription beanDesc)
    {
        return new LayeredPropertyWriterBuilder(config, beanDesc);
    }

    @Override
    public SerializerFactory withConfig(SerializerFactoryConfig config) {
        if (_factoryConfig == config) {
            return this;
        }
        return new LayeredBeanSerializerFactory(config);
    }
}
