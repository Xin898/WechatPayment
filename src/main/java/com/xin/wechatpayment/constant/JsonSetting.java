package com.xin.wechatpayment.constant;


public enum JsonSetting {
    private static  final SerializeConfig serializeConfig;

    private static final ParserConfig parserConfig;

    /**
     * @link https://github.com/alibaba/fastjson/wiki/PropertyNamingStrategy_cn
     */
    static {
        serializeConfig = new SerializeConfig();
        serializeConfig.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        parserConfig = new ParserConfig();
        parserConfig.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
    }

    public static SerializeConfig getSerializeConfig(){
        return serializeConfig;
    }

    public static ParserConfig getParserConfig(){
        return parserConfig;
    }
}
