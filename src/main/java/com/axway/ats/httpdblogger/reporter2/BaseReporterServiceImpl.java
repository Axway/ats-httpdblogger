/*
 * Copyright 2020 Axway Software
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
package com.axway.ats.httpdblogger.reporter2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.axway.ats.core.utils.StringUtils;
import com.axway.ats.httpdblogger.reporter2.utils.DbReader;
import com.axway.ats.httpdblogger.reporter2.utils.DbReader.CompareSign;

public class BaseReporterServiceImpl {

    protected Map<String, List<Pair<CompareSign, Object>>>
            constructWhereClauseMap( String whereClauseKeys, String whereClauseCmpSigns, String whereClauseValues ) {

        Map<String, List<Pair<CompareSign, Object>>> map = new HashMap<String, List<Pair<CompareSign, Object>>>();

        final String DELIMITER = ",";
        boolean hasKeys = !StringUtils.isNullOrEmpty(whereClauseKeys);
        boolean hasCmpSigns = !StringUtils.isNullOrEmpty(whereClauseCmpSigns);
        boolean hasValues = !StringUtils.isNullOrEmpty(whereClauseValues);

        if (!hasKeys) {
            if (!hasCmpSigns) {
                if (!hasValues) {
                    // empty where clause, return empty map
                    return map;
                } else {
                    throw new RuntimeException("where clause values not found!");
                }
            } else {
                throw new RuntimeException("where clause compare signs not found!");
            }
        } else {
            if (!hasCmpSigns) {
                throw new RuntimeException("where clause compare signs not found!");
            }
            if (!hasValues) {
                throw new RuntimeException("where clause values not found!");
            }
        }

        String[] keys = whereClauseKeys.split(DELIMITER);
        String[] signs = whereClauseCmpSigns.split(DELIMITER);
        String[] values = whereClauseValues.split(DELIMITER);

        if (keys.length != values.length || keys.length != signs.length || signs.length != values.length) {
            throw new RuntimeException("where clause must have equal number of keys, compare signs and values!");
        }

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i].trim();
            String value = values[i].trim();
            String sign = signs[i].trim();
            if (map.containsKey(key)) {
                map.get(key).add(new ImmutablePair<DbReader.CompareSign, Object>(CompareSign.fromString(sign), value));
            } else {
                List<Pair<CompareSign, Object>> array = new ArrayList<Pair<CompareSign, Object>>();
                array.add(new ImmutablePair<DbReader.CompareSign, Object>(CompareSign.fromString(sign), value));
                map.put(key, array);
            }

        }

        return map;
    }

}
