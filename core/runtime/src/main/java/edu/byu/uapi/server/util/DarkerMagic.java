package edu.byu.uapi.server.util;

import edu.byu.uapi.server.scalars.EnumScalarType;
import edu.byu.uapi.spi.scalars.ScalarType;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;

public class DarkerMagic {
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <Type> ScalarType<Type> getEnumScalarConverter(@Nonnull KClass<Type> type) {
        final Class<Type> javaType = JvmClassMappingKt.getJavaClass(type);
        if (!javaType.isEnum()) {
            throw new IllegalArgumentException(javaType + " is not an enum type");
        }
        return new EnumScalarType((Enum[]) javaType.getEnumConstants());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <Type> Comparator<Type> maybeNaturalComparatorFor(@Nonnull KClass<Type> type) {
        if (Comparable.class.isAssignableFrom(JvmClassMappingKt.getJavaClass(type))) {
            return (Comparator<Type>) Comparator.naturalOrder();
        } else {
            return null;
        }
    }
}
