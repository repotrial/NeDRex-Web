package de.exbio.reposcapeweb.communication.reponses;

import de.exbio.reposcapeweb.filter.FilterType;

public class Suggestion {
        private String text;
        private String value;
        private String sid;
        private String type;
        private String matcher;
        private boolean distinct;
        private int size;
        private Integer targetCount;

        public Suggestion(String type, String name) {
            this.type = type;
            this.text = name;
            this.value = name + " [" + type + "]";
        }

        public Suggestion(FilterType type, boolean distinct, String name, String key, int id, int size) {
            this(type.name(), name);
            this.matcher = key;
            this.size = size;
            this.sid = !distinct ? id + "" : type.ordinal() + "_" + id;
        }

        public String getText() {
            return text;
        }

        public String getValue() {
            return value;
        }

        public String getType() {
            return type;
        }

        public String getKey() {
            return matcher;
        }

        public String getSId() {
            return sid;
        }

        public Integer getTargetCount() {
            return targetCount;
        }

        public void setTargetCount(int targetCount) {
            this.targetCount = targetCount;
        }

        public boolean isDistinct() {
            return distinct;
        }

        public int getSize() {
            return size;
        }
}
