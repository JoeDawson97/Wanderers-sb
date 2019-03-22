package com.wanderers.wanderers.sys.base;

import lombok.Data;

@Data
public class BaseModel {

    protected Long id;
    protected String delFlag;

    public final static String NORMAL="0";
    public final static String DELETED="1";

}
