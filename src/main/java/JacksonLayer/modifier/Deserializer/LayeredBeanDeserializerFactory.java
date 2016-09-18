package JacksonLayer.modifier.Deserializer;

import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;

/**
 * Created by bresai on 16/9/13.
 */
public class LayeredBeanDeserializerFactory extends BeanDeserializerFactory {
    public LayeredBeanDeserializerFactory(DeserializerFactoryConfig config) {
        super(config);
    }

//@   Override
//    protected BeanDeserializerBuilder constructBeanDeserializerBuilder(DeserializationContext ctxt,
//                                                                       BeanDescription beanDesc,
//                                                                       LayeredSerializer serializer) {
//        return new LayeredBeanDeserializerBuilder(beanDesc, ctxt.getConfig(), fields);
//    }

    @Override
    public DeserializerFactory withConfig(DeserializerFactoryConfig config) {
        if (_factoryConfig == config) {
            return this;
        }
        return new LayeredBeanDeserializerFactory(config);
    }
}
