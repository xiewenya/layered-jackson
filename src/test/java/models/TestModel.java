package models;

import java.io.Serializable;

/**
 * Created by bresai on 16/9/13.
 */
public class TestModel implements Serializable {
        private Integer number;

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

        public TestModel() {
        }
}
