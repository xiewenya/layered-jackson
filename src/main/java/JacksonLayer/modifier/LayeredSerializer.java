package JacksonLayer.modifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bresai on 16/9/5.
 */
public abstract class LayeredSerializer {

    public List<String> ignoredProps;

    public List<String> includedProps;

    public void setIgnoredProps(List<String> ignoredProps) {
        this.ignoredProps = ignoredProps;
    }

    public void setIncludedProps(List<String> includedProps) {
        this.includedProps = includedProps;
    }

    public List<String> getIgnoredProps() {
        if (ignoredProps == null){
            ignoredProps = new ArrayList<String>();
        }
        return ignoredProps;
    }

    public List<String> getIncludedProps() {
        if (includedProps == null){
            this.includedProps = new ArrayList<String>();
        }
        return includedProps;
    }

    public LayeredSerializer() {
    }
}
