/*
 * Copyright 2019 Axway Software
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.axway.ats.httpdblogger.reporter2.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.axway.ats.core.reflect.ReflectionUtils;

public class JsonUtils {

    /**
     * Constructs a JSON request for monitoring operations
     * @param keys the JSON keys
     * @param values the JSON values
     * @param isTopLevelObject whether to enclose the whole JSON in parenthesis {} (true) or not (false)
     * */
    public static String constructJson(
                                        String[] keys,
                                        Object[] values,
                                        boolean isTopLevelObject ) throws IllegalArgumentException {

        if (keys != null) {
            if (values != null) {
                if (keys.length != values.length) {
                    throw new IllegalArgumentException("The number of keys is " + keys.length
                                                       + ", but we got " + values.length
                                                       + " values instead. Please consult the documentation.");
                }
            } else {
                throw new IllegalArgumentException("values are null");
            }
        } else {
            throw new IllegalArgumentException("keys are null");
        }

        StringBuilder sb = new StringBuilder();
        if (isTopLevelObject) {
            sb.append("{");
        }

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            Object value = values[i];
            sb.append("\"").append(key).append("\"").append(":").append(toJson(value)).append(",");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // remove trailing comma
        }

        if (isTopLevelObject) {
            sb.append("}");
        }

        return sb.toString();

    }

    /** Convert JAVA object to JSON.<br/>
     * Objects that are handled automatically are <strong>primitives</strong> and instances of <strong>{@link CharSequence}, {@link Set}, {@link Map}, {@link List} or arrays</strong>.</br>
     * Other objects will be serialized by returning each of their members, regardless of the access type. 
     * If the object has parent class, its members <strong>WILL NOT</strong> be included
     * @param value - the JAVA object
     * @return JSON representation of the JAVA object
     */
    public static String toJson( Object value ) {

        StringBuilder sb = new StringBuilder();
        
        if(value == null) {
            return "null";
        }

        if (value instanceof CharSequence) {
            sb.append("\"").append(value.toString()).append("\"");
        } else if (value instanceof Boolean) {
            sb.append(value.toString());
        } else if (value instanceof Number) {
            sb.append(value.toString());
        } else if (value instanceof Character) {
            sb.append("\"").append(value.toString()).append("\"");
        } else if (value instanceof List<?>) {
            return toJson( ((List) value).toArray());
        } else if (value instanceof Set<?>) {
            return toJson( ((Set) value).toArray());
        } else if (value instanceof Map<?, ?>) {
            sb.append("{");
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                sb.append(toJson(entry.getKey())).append(":").append(toJson(entry.getValue())).append(",");
            }
            if (sb.length() > 1) {
                sb.setLength(sb.length() - 1);// remove trailing comma
            }
            sb.append("}");
        } else if (value.getClass().isArray()) {
            sb.append("[");
            for (int i = 0; i < Array.getLength(value); i++) {
                Object el = Array.get(value, i);
                sb.append(toJson(el)).append(",");
            }
            if (sb.length() > 1) {
                sb.setLength(sb.length() - 1); // remove trailing comma
            }
            sb.append("]");
        } else {
            // TODO - json-fy all members (but what about the parent one?)
            sb.append("{");
            List<Field> fields = ReflectionUtils.getAllFields(value, false);
            for (Field f : fields) {
                String name = f.getName();
                sb.append(toJson(ReflectionUtils.getFieldValue(value, name, false)));
            }
            if (sb.length() > 1) {
                sb.setLength(sb.length() - 1); // remove trailing comma
            }
            sb.append("}");
        }

        return sb.toString();
    }

}
