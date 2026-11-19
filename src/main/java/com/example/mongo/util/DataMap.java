package com.example.mongo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

public class DataMap extends LinkedHashMap<String, Object> implements Serializable {
    private static final long serialVersionUID = 8180203168113225298L;
    private static final Logger LOG = LoggerFactory.getLogger(DataMap.class);
    private String name = null;
    private int dataMapSize = 0;

    /**
     * .
     */
    public DataMap() {
        super(500);
        dataMapSize = 500;
    }

    public DataMap(int size) {
        super(size);
    }

    public DataMap(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void set(String key, Object value) {
        super.put(key, value);
    }

    public Object get(String name) {
        return (Object) super.get(name);
    }

    /**
     * Map data 를 담는다.
     *
     * @param name  arg0
     * @param value arg1
     */
    public void put(String name, String value) {
        if (value == null) {
            super.put(name, "");
        } else {
            super.put(name, new String(value));
        }
    }

    public void put(String name, int value) {
        super.put(name, value);
    }

    public void put(String name, long value) {
        super.put(name, value);
    }

    public void put(String name, float value) {
        super.put(name, value);
    }

    public void put(String name, double value) {
        super.put(name, value);
    }

    public void put(String name, boolean value) {
        super.put(name, value);
    }

    /**
     * Map data 에서 string 형식을 반환한다.
     *
     * @param name key
     * @return String
     */
    public String getString(String name) {
        Object obj = super.get(name);

        if (obj == null) {
            return null;
        }

        if ((obj instanceof Collection<?>) || (obj instanceof Object[])) {
            LOG.info("A value type does not match String data type.");
        }

        return obj.toString();
    }

    /**
     * Map data 에서 string 형식을 반환한다.
     *
     * @param name         key
     * @param defaultValue value
     * @return String
     */
    public String getString(String name, String defaultValue) {
        String str = getString(name);

        if (str == null || str.isEmpty()) {
            return defaultValue;
        }

        return str;
    }

    public int getInt(String name) {
        String value = getString(name);
        return ((value == null || value.isEmpty()) ? 0 : Integer.parseInt(value));
    }

    public int getInt(String name, int defaultValue) {
        String value = getString(name);
        return ((value == null || value.isEmpty()) ? defaultValue : Integer.parseInt(value));
    }

    public long getLong(String name) throws NumberFormatException, Exception {
        String value = getString(name);
        return (value == null ? 0L : Long.parseLong(value));
    }

    /**
     * Map data 에서 boolean 형식을 반환한다.
     *
     * @param name key
     * @return boolean
     */
    public boolean getBoolean(String name) {
        String value = getString(name);

        if (value == null) {
            return false;
        } else {
            boolean isBlank = false;

            if (value == null || value.trim().length() <= 0) {
                isBlank = true;
            }

            if (isBlank) {
                return false;
            } else {
                return value.equalsIgnoreCase("true");
            }
        }
    }

    /**
     * Map data 에서 double 형식을 반환한다.
     *
     * @param name key
     * @return double
     */
    public double getDouble(String name) {
        Object object = super.get(name);

        if (object != null) {
            Class<?> clazz = object.getClass();

            if (clazz == Double.class) {
                return (Double) object;
            } else if (clazz == Float.class) {
                return (Float) object;
            } else if ((clazz == String.class) || (clazz == BigDecimal.class)) {
                try {
                    return Double.parseDouble(object.toString());
                } catch (NumberFormatException ex) {
                    LOG.info("A value type does not match Double data type.");
                }
            }
        }

        return 0.0D;
    }

    /**
     * Map data 에서 float 형식을 반환한다.
     *
     * @param name key
     * @return float
     */
    public float getFloat(String name) {
        Object object = super.get(name);

        if (object != null) {
            Class<?> clazz = object.getClass();

            if (clazz == Float.class) {
                return (Float) object;
            } else if ((clazz == String.class) || (clazz == BigDecimal.class)) {
                try {
                    return Float.parseFloat(object.toString());
                } catch (NumberFormatException ex) {
                    LOG.info("A value type does not match Float data type.");
                }
            }
        }

        return 0.0F;
    }

    /**
     * Map data 에서 byte[] 형식을 반환한다.
     *
     * @param name key
     * @return byte[]
     */
    public byte[] getBinary(String name) {
        byte[] value = null;

        if (name != null && this.containsKey(name)) {
            value = (byte[]) (super.get(name));
        } else {
            LOG.info("A DataMap does not contain this key.");
        }

        return value;
    }

    /**
     * Map 을 string 형식으로 변환하여 반환한다.
     *
     * @return String
     */
    public String toString() {
        if (isEmpty()) {
            return "Data does not exist.";
        }

        StringBuilder sb = new StringBuilder();
        Set<String> keySet = super.keySet();
        Iterator<String> iter = keySet.iterator();
        String key = null;
        Object item = null;

        sb.append("\n\n----------------------------[DataMap Start]----------------------------\n\n");

        while (iter.hasNext()) {
            key = iter.next();
            item = get(key);

            if (item == null) {
                sb.append(key).append("=null\n");
            } else if (item instanceof String) {
                sb.append(key).append("=[ ").append(item).append(" ]\n");
            } else if ((item instanceof Integer) || (item instanceof Long) || (item instanceof Double)
                    || (item instanceof Float) || (item instanceof Boolean)) {
                sb.append(key).append("=[ ").append(item).append(" ]\n");
            } else if (item instanceof String[] data) {
                sb.append(key).append("=[ ");

                for (String datum : data) {
                    sb.append(datum).append(',');
                }

                sb.append("] Array Size:").append(data.length).append(" \n");
            } else {
                sb.append(key).append("=[ ").append(item).append(" ] ClassName:").append(item.getClass().getName()).append("\n");
            }
        }

        sb.append("\n----------------------------[DataMap End]----------------------------");

        return sb.toString();
    }

    /**
     * Map 을 string 형식으로 변환하여 반환한다. (dmresult 상세 표출을 위해 일부 수정)
     *
     * @return String
     */
    public String toStringDmResult() {
        if (isEmpty()) {
            return "Data does not exist.";
        }

        StringBuilder sb = new StringBuilder();
        Set<String> keySet = super.keySet();
        Iterator<String> iter = keySet.iterator();
        String key = null;
        Object item = null;

        //sb.append("\n\n----------------------------[DataMap Start]----------------------------\n\n");

        while (iter.hasNext()) {
            key = iter.next();
            item = get(key);

            if (item == null) {
                sb.append(key).append("=null\n");
            } else if (item instanceof String) {
                sb.append(key).append("=[ ").append(item).append(" ]\n");
            } else if ((item instanceof Integer) || (item instanceof Long) || (item instanceof Double)
                    || (item instanceof Float) || (item instanceof Boolean)) {
                sb.append(key).append("=[ ").append(item).append(" ]\n");
            } else if (item instanceof String[] data) {
                sb.append(key).append("=[ ");

                for (String datum : data) {
                    sb.append(datum).append(',');
                }

                sb.append("]\n");
            } else if (item instanceof List) { //ArrayList 추가 - 230403 CJE
                sb.append(key).append("=").append(item).append("\n");
            } else {
                sb.append(key).append("=[ ").append(item).append(" ]\n");
            }
        }

        //sb.append("\n----------------------------[DataMap End]----------------------------");

        return sb.toString();
    }


    @Override
    protected boolean removeEldestEntry(Entry<String, Object> eldest) {
        boolean isRemove = size() > dataMapSize;

        if (isRemove) {
            Object object = this.get(eldest.getKey());
            if (object instanceof BufferedWriter) {
                try {
                    ((BufferedWriter) object).close();
                } catch (IOException e) {
                    LOG.info("BufferedWriter IOException ");
                }
            }
            LOG.error("removeEldestEntry -> eldest.getKey() : {}", eldest.getKey());
            this.remove(eldest.getKey());
        }
        return isRemove;
    }
}
