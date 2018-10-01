package edu.byu.uapi.server.scalars;

import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;

public class EnumScalarConverterHelper {
    @SuppressWarnings("unchecked")
    public static <Type> ScalarConverter<Type> getEnumScalarConverter(KClass<Type> type) {
        final Class<Type> javaType = JvmClassMappingKt.getJavaClass(type);
        if (!javaType.isEnum()) {
            throw new IllegalArgumentException(javaType + " is not an enum type");
        }
        return new EnumScalarConverter((Enum[]) javaType.getEnumConstants());
    }
}
