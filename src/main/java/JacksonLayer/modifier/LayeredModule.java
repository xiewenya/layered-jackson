package JacksonLayer.modifier;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

/**
 * Created by bresai on 16/9/5.
 */
public class LayeredModule extends SimpleModule {
    protected BeanSerializerModifier serializerModifier;
    protected BeanDeserializerModifier deserializerModifier;

    public LayeredModule(BeanSerializerModifier modifier)
    {
        super("test", Version.unknownVersion());
        this.serializerModifier = modifier;
        this.deserializerModifier = null ;
    }

    public LayeredModule(BeanDeserializerModifier modifier)
    {
        super("test", Version.unknownVersion());
        this.serializerModifier = null;
        this.deserializerModifier = modifier ;
    }

    public LayeredModule(BeanSerializerModifier modifier, BeanDeserializerModifier demodifier)
    {
        super("test", Version.unknownVersion());
        this.serializerModifier = modifier;
        this.deserializerModifier = demodifier;
    }

    @Override
    public void setupModule(SetupContext context)
    {
        super.setupModule(context);
        if (serializerModifier != null) {
            context.addBeanSerializerModifier(serializerModifier);
        }
        if (deserializerModifier != null) {
            context.addBeanDeserializerModifier(deserializerModifier);
        }
    }
    
    
}
