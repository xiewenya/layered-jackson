import JacksonLayer.fields.ChoiceField;
import JacksonLayer.fields.ExtraField;
import JacksonLayer.fields.Field;
import JacksonLayer.fields.IntegerField;
import JacksonLayer.modifier.Deserializer.LayeredDeserializerModifier;
import JacksonLayer.modifier.LayeredModule;
import JacksonLayer.modifier.LayeredObjectMapper;
import JacksonLayer.modifier.LayeredSerializer;
import JacksonLayer.modifier.Serializer.LayeredSerializerModifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import junit.framework.TestCase;
import models.TestModel;
import models.TestModel2;

import java.io.IOException;
import java.util.*;

//import JacksonLayer.modifier.Serializer.LayeredBeanSerializerFactory;

/**
 * Created by bresai on 16/8/30.
 */
public class SerializerAndDeserializerTest extends TestCase {

    public class TestNormal extends LayeredSerializer {
        private Field number;
    }

    public void testNormalSerialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
//        mapper.setSerializerFactory(new LayeredBeanSerializerFactory(null));
        mapper.registerModule(new LayeredModule(new LayeredSerializerModifier(new TestNormal()),new LayeredDeserializerModifier(new TestNormal())));
        TestModel model = new TestModel();
        model.setNumber(1);
        String test = mapper.writeValueAsString(model);
        assertEquals(test, "{\"number\":1}");

        TestModel result = mapper.readValue(test, TestModel.class);

        assertEquals(result.getNumber(), model.getNumber());
    }

    public class TestDefaultsAndLabel extends LayeredSerializer {
        private Field number = new IntegerField.Builder().setDefaultValue(12).setLabel("changedName").build();
    }

    public void testDefaultsAndLabelSerializer() throws IOException {
        //serializer
        ObjectMapper mapper = LayeredObjectMapper.createInstance(new TestDefaultsAndLabel());
        TestModel model = new TestModel();
        String test = mapper.writeValueAsString(model);
        assertEquals("{\"changedName\":12}", test);

        //to JsonNode
        JsonNode jsonNode = mapper.valueToTree(model);
        assertEquals(12, jsonNode.get("changedName").asInt());

        //deserializer
        TestModel result = mapper.readValue(test, TestModel.class);
        model.setNumber(12);
        assertEquals(result.getNumber(), model.getNumber());

        // from JsonNode
        result = mapper.treeToValue(jsonNode, TestModel.class);
        assertEquals(result.getNumber(), model.getNumber());

        //list serializer
        List<TestModel> list = new ArrayList<TestModel>();
        list.add(model);
        TestModel model1 = new TestModel();
        model1.setNumber(11);
        list.add(model1);
        test = mapper.writeValueAsString(list);
        assertEquals("[{\"changedName\":12},{\"changedName\":11}]", test);

        //to ArrayNode
        ArrayNode arrayNode = mapper.valueToTree(list);
        assertEquals(2, arrayNode.size());
        assertEquals(12, arrayNode.get(0).get("changedName").asInt());
        assertEquals(11, arrayNode.get(1).get("changedName").asInt());
    }

    public class TestWriteOnly extends LayeredSerializer {
        private Field number = new IntegerField.Builder().setWriteOnly(true).build();
    }

    public void testWriteOnlySerializer() throws IOException {
        //serializer
        ObjectMapper mapper = LayeredObjectMapper.createInstance(new TestWriteOnly());
        TestModel model = new TestModel();
        String test = mapper.writeValueAsString(model);
        assertEquals("{}", test);

        //to JsonNode
        JsonNode node = mapper.valueToTree(model);
        assertEquals(null, node.get("number"));

        //list serializer
        List<TestModel> list = new ArrayList<TestModel>();
        list.add(model);
        TestModel model1 = new TestModel();
        model1.setNumber(11);
        list.add(model1);
        test = mapper.writeValueAsString(list);
        assertEquals("[{},{}]", test);

        //to ArrayNode
        ArrayNode arrayNode = mapper.valueToTree(list);
        assertEquals(2, arrayNode.size());
        assertEquals(null, arrayNode.get(0).get("number"));
        assertEquals(null, arrayNode.get(1).get("number"));
    }

    public class TestReadOnly extends LayeredSerializer {
        private Field number = new IntegerField.Builder().setReadOnly(true).build();
    }

    public void testReadOnlySerializer() throws IOException {
        ObjectMapper mapper = LayeredObjectMapper.createInstance(new TestReadOnly());
        String test = "{\"number\":12}";
        TestModel result = mapper.readValue(test, TestModel.class);
        assertEquals(result.getNumber(), null);
    }

    private enum WeekDayEnum{
        Mon(1), Tue(2), Wed(3), Thu(4), Fri(5), Sat(6), Sun(7);

        private int index;

        WeekDayEnum(int idx) {
            this.index = idx;
        }

        public int getIndex() {
            return index;
        }

        public static Map<Integer, String> getMap(){
            Map<Integer, String> map = new HashMap<Integer, String>();
            for (WeekDayEnum choice : WeekDayEnum.values()){
                map.put(choice.getIndex(), choice.name());
            }
            return map;
        }
    }

    public class TestChoice extends LayeredSerializer {
        private Field number = new ChoiceField.Builder().setChoices(WeekDayEnum.getMap()).build();
    }

    public void testChoicesSerializer() throws IOException {
        //serializer
        ObjectMapper mapper = LayeredObjectMapper.createInstance(new TestChoice());
        TestModel model = new TestModel();
        model.setNumber(2);
        String test = mapper.writeValueAsString(model);
        assertEquals("{\"number\":\"Tue\"}", test);

        //to JsonNode
        JsonNode jsonNode = mapper.valueToTree(model);
        assertEquals("Tue", jsonNode.get("number").asText());

        //deserializer
        TestModel result = mapper.readValue(test, TestModel.class);
        assertEquals(result.getNumber().toString(), "2");

        // from JsonNode
        result = mapper.treeToValue(jsonNode, TestModel.class);
        assertEquals("2", result.getNumber().toString());

        List<TestModel> list = new ArrayList<TestModel>();
        list.add(model);
        TestModel model1 = new TestModel();
        model1.setNumber(3);
        list.add(model1);

        //to ArrayNode
        ArrayNode arrayNode = mapper.valueToTree(list);
        assertEquals(2, arrayNode.size());
        assertEquals("Tue", arrayNode.get(0).get("number").asText());
        assertEquals("Wed", arrayNode.get(1).get("number").asText());
    }

    private enum WeekDayEnum2{
        Mon("a"), Tue("b"), Wed("c"), Thu("d"), Fri("e"), Sat("f"), Sun("g");

        private String index;

        WeekDayEnum2(String idx) {
            this.index = idx;
        }

        public String getIndex() {
            return index;
        }

        public static Map<String, String> getMap(){
            Map<String, String> map = new HashMap<String, String>();
            for (WeekDayEnum2 choice : WeekDayEnum2.values()){
                map.put(choice.getIndex(), choice.name());
            }
            return map;
        }

    }

    public class TestChoice2 extends LayeredSerializer {
        private Field string = new ChoiceField.Builder().setChoices(WeekDayEnum2.getMap()).build();
    }

    public void testChoices2Serializer() throws IOException {
        ObjectMapper mapper = LayeredObjectMapper.createInstance(new TestChoice2());
        TestModel2 model = new TestModel2();
        model.setString("a");
        String test = mapper.writeValueAsString(model);
        assertEquals("{\"string\":\"Mon\"}", test);
        TestModel2 result = mapper.readValue(test, TestModel2.class);
        assertEquals(result.getString(), "a");
    }


    public class TestCustomized extends LayeredSerializer {
        private Field number = new IntegerField.Builder()
                .setSerMethod("testSer")
                .setDeserMethod("testDeser")
                .build();

        public Object testSer(Object object){
            Integer number = (Integer) object;
            System.out.println(number);
            return number + 1;
        }

        public Object testDeser(Object object){
            if (object instanceof String){
                Integer number = Integer.parseInt((String) object);

                System.out.println(object);
                return number - 1;
            }
            else if (object instanceof Integer){
                Integer number = (Integer) object;

                System.out.println(object);
                return number - 1;
            }
            return object;
        }
    }

    public void testCustomizedSerializer() throws IOException {
        //serializer
        ObjectMapper mapper = LayeredObjectMapper.createInstance(new TestCustomized());
        TestModel model = new TestModel();
        model.setNumber(12);
        String test = mapper.writeValueAsString(model);
        assertEquals("{\"number\":13}", test);

        //to json
        JsonNode jsonNode = mapper.valueToTree(model);
        assertEquals(13, jsonNode.get("number").asInt());

        //list serializer
        List<TestModel> list = new ArrayList<TestModel>();
        list.add(model);
        TestModel model2 = new TestModel();
        model2.setNumber(123);
        list.add(model2);
        test = mapper.writeValueAsString(list);
        assertEquals("[{\"number\":13},{\"number\":124}]", test);

        //deserializer
        mapper = LayeredObjectMapper.createInstance(new TestCustomized());
        TestModel result = mapper.readValue("{\"number\":\"12\"}", TestModel.class);
        assertEquals(result.getNumber().toString(), "11");

        //from json
        result = mapper.treeToValue(jsonNode, TestModel.class);
        assertEquals("12", result.getNumber().toString());

    }

    public class TestAdditionalField extends LayeredSerializer {
        private Field test = new ExtraField.Builder().setSerMethod("test").build();

        public Object test(Object object){
            if (object instanceof TestModel){
                return ((TestModel)object).getNumber()+2;
            } else{
                return "haha";
            }
        }
    }

    public void testAdditionalField() throws IOException {
        //serializer
        ObjectMapper mapper = LayeredObjectMapper.createInstance(new TestAdditionalField());
        TestModel model = new TestModel();
        model.setNumber(1);
        String test = mapper.writeValueAsString(model);
        assertEquals("{\"number\":1,\"test\":3}", test);

        //to json
        JsonNode jsonNode = mapper.valueToTree(model);
        assertEquals(1, jsonNode.get("number").asInt());
        assertEquals(3, jsonNode.get("test").asInt());

        //list Serializer
        List<TestModel> list = new ArrayList<TestModel>();
        list.add(model);
        TestModel model1 = new TestModel();
        model1.setNumber(123);
        list.add(model1);

        test = mapper.writeValueAsString(list);
        assertEquals("[{\"number\":1,\"test\":3},{\"number\":123,\"test\":125}]", test);

        //to arrayNode
        ArrayNode arrayNode = mapper.valueToTree(list);
        assertEquals(2, arrayNode.size());
        assertEquals(3, arrayNode.get(0).get("test").asInt());
        assertEquals(125, arrayNode.get(1).get("test").asInt());
    }

    public void testIncludeAndIgnoreField() throws IOException {
        LayeredSerializer serializer = new TestAdditionalField();
        ObjectMapper mapper = LayeredObjectMapper.createInstance(serializer);
        serializer.setIgnoredProps(Collections.singletonList("test"));

        TestModel model = new TestModel();
        model.setNumber(1);
        String test = mapper.writeValueAsString(model);
        assertEquals("{\"number\":1}", test);


        //if only extra fields are included, you need to change configuration
        serializer.setIgnoredProps(Collections.<String>emptyList());
        serializer.setIncludedProps(Collections.singletonList("test"));
        mapper = LayeredObjectMapper.createInstance(serializer);

        model = new TestModel();
        model.setNumber(1);
        test = mapper.writeValueAsString(model);
        assertEquals("{\"test\":3}", test);
    }

}
