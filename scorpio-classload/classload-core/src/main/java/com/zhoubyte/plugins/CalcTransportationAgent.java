package com.zhoubyte.plugins;

import com.zhoubyte.enums.PluginsType;

public class CalcTransportationAgent implements CalcInterface{

    private final static PluginsType type = PluginsType.TRANSPORTATION;

    @Override
    public Double calc(Double baseMoney) {
        if(PluginsType.TRANSPORTATION == type) {
            return baseMoney * 0.5;
        }
        return 0.0;
    }
}
