package com.beyond.note5.utils.converter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: beyond
 * @date: 2019/2/3
 */

public class ConverterBuilder<T, S> {

    private List<Converter> converters = new LinkedList<>();

    @SuppressWarnings("unchecked")
    public ConverterBuilder addConverter(Converter converter) {

        if (converters.isEmpty()) {
            converters.add(converter);
            return this;
        }
        Converter addedConvert = converters.get(converters.size() - 1);
        Type[] addedConvertArguments = ((ParameterizedType) addedConvert.getClass().getGenericInterfaces()[0]).getActualTypeArguments();
        Class addedConvertArgument0 = (Class) addedConvertArguments[0];
        Class addedConvertArgument1 = (Class) addedConvertArguments[1];

        Type[] converterArguments = ((ParameterizedType) converter.getClass().getGenericInterfaces()[0]).getActualTypeArguments();
        Class converterArgument0 = (Class) converterArguments[0];
        Class converterArgument1 = (Class) converterArguments[1];

        if (converterArgument0.isAssignableFrom(addedConvertArgument1)) {
            converters.add(converter);
        } else {
            throw new IllegalArgumentException("转化器添加错误");
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public Converter<T, S> build() {
        return new Converter<T, S>() {
            @Override
            public S convert(T t) {
                Object tmp = t;
                for (Converter converter : converters) {
                    tmp = converter.convert(tmp);
                }
                return (S) tmp;
            }
        };
    }
}
