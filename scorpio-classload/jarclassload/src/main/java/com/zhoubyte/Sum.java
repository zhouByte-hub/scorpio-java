package com.zhoubyte;

public class Sum {

    public Double sum(Double ...x){
        Double sum = 0.0;
        for (Double v : x) {
            sum += v;
        }
        return sum;
    }
}
