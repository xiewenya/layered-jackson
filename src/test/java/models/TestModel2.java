package models;

import java.io.Serializable;

/**
 * Created by bresai on 16/9/13.
 */
public class TestModel2 implements Serializable {
    private String string;

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public TestModel2() {
    }
}


