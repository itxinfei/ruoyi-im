package com.ruoyi.im.config;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 字符串到整数的类型处理器
 * 用于处理数据库中存储为字符串但需要映射为整数的字段
 *
 * @author ruoyi
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(Integer.class)
public class StringToIntegerTypeHandler implements TypeHandler<Integer> {

    @Override
    public void setParameter(PreparedStatement ps, int i, Integer parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, java.sql.Types.INTEGER);
        } else {
            ps.setInt(i, parameter);
        }
    }

    @Override
    public Integer getResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return convertStringToInteger(value);
    }

    @Override
    public Integer getResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return convertStringToInteger(value);
    }

    @Override
    public Integer getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return convertStringToInteger(value);
    }

    private Integer convertStringToInteger(String value) {
        if (value == null || value.isEmpty()) {
            return 0; // 默认值
        }
        
        // 处理常见的状态字符串
        switch (value.toUpperCase()) {
            case "ACTIVE":
            case "ENABLE":
            case "NORMAL":
                return 0; // 活跃状态通常对应 0
            case "INACTIVE":
            case "DISABLE":
            case "FORBIDDEN":
            case "BANNED":
                return 1; // 禁用状态通常对应 1
            default:
                try {
                    // 尝试直接转换为整数
                    return Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    // 如果无法转换，返回默认值
                    return 0;
                }
        }
    }
}