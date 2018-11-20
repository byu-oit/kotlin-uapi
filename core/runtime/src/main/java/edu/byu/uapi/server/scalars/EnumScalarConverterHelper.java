package edu.byu.uapi.server.scalars;

import edu.byu.uapi.spi.scalars.ScalarType;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;

import javax.annotation.Nonnull;

public class EnumScalarConverterHelper {
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <Type> ScalarType<Type> getEnumScalarConverter(@Nonnull KClass<Type> type) {
        final Class<Type> javaType = JvmClassMappingKt.getJavaClass(type);
        if (!javaType.isEnum()) {
            throw new IllegalArgumentException(javaType + " is not an enum type");
        }
        return new EnumScalarType((Enum[]) javaType.getEnumConstants());
    }
}
