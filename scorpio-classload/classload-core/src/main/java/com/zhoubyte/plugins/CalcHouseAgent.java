package com.zhoubyte.plugins;

import com.zhoubyte.enums.PluginsType;

public class CalcHouseAgent implements CalcInterface {

    private static final PluginsType type = PluginsType.HOUSE;

    @Override
    public Double calc(Double baseMoney) {
        if(PluginsType.HOUSE == type) {
            return (baseMoney + 200) * 1.2;
        }
        return 0.0;
    }
}
