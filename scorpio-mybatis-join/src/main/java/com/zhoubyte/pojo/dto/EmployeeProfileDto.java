package com.zhoubyte.pojo.dto;

import icu.mhb.mybatisplus.plugln.annotations.conditions.Like;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeProfileDto {

    /**
     * 未指定 tableAlias 时使用主表别名，这里是 employee.name。
     */
    @Like(mappingColum = "name")
    private String employeeName;

}
