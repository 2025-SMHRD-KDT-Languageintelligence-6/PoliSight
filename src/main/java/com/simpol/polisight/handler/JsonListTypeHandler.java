package com.simpol.polisight.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpol.polisight.type.CodeEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class JsonListTypeHandler<E extends Enum<E> & CodeEnum> extends BaseTypeHandler<List<E>> {
    private final Class<E> type;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonListTypeHandler(Class<E> type) {
        if (type == null) throw new IllegalArgumentException("Type argument cannot be null");
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<E> parameter, JdbcType jdbcType) throws SQLException {
        try {
            // List<Enum> -> ["0011001", "0011005"] 형태의 문자열로 변환
            List<String> codes = parameter.stream().map(CodeEnum::getCode).collect(Collectors.toList());
            ps.setString(i, objectMapper.writeValueAsString(codes));
        } catch (Exception e) {
            throw new SQLException("Error converting list to JSON", e);
        }
    }

    @Override
    public List<E> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toJson(rs.getString(columnName));
    }

    @Override
    public List<E> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toJson(rs.getString(columnIndex));
    }

    @Override
    public List<E> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toJson(cs.getString(columnIndex));
    }

    private List<E> toJson(String json) {
        if (json == null || json.isEmpty()) return Collections.emptyList();
        try {
            List<String> codes = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            return codes.stream()
                    .map(code -> Arrays.stream(type.getEnumConstants())
                            .filter(e -> e.getCode().equals(code))
                            .findFirst()
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}