package JacksonLayer.utils;

import java.util.List;
import java.util.Map;

/**
 * Created by bresai on 16/9/18.
 */
public class utils {

    public static Boolean isEmpty(String string){
        return string == null || string.isEmpty();
    }

    public static Boolean isNotEmpty(String string){
        return ! isEmpty(string);
    }

    public static Boolean isEmpty(Map map){
        return map == null || map.isEmpty();
    }

    public static Boolean isNotEmpty(Map map){
        return ! isEmpty(map);
    }

    public static Boolean isEmpty(List list){
        return list == null || list.isEmpty();
    }

    public static Boolean isNotEmpty(List list){
        return ! isEmpty(list);
    }
}
