import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collection;
//
//public class SimpleTestFilters implements TestFilters {
//    @Nullable
//    private final String aString;
//    @Nullable
//    private final Integer anInt;
//    @NotNull
//    private final Collection<Integer> multiple;
//    @Nullable
//    private final Nested nested;
//
//    public SimpleTestFilters(@Nullable String aString, @Nullable Integer anInt, @NotNull Collection<Integer> multiple, @Nullable Nested nested) {
//        this.aString = aString;
//        this.anInt = anInt;
//        this.multiple = multiple;
//        this.nested = nested;
//    }
//
//    @Nullable
//    public String getAString() {
//        return aString;
//    }
//
//    @javax.annotation.Nullable
//    @Nullable
//    public Integer getAnInt() {
//        return anInt;
//    }
//
//    @Nonnull
//    @NotNull
//    public Collection<Integer> getMultiple() {
//        return multiple;
//    }
//
//    @javax.annotation.Nullable
//    @Nullable
//    public Nested getNested() {
//        return nested;
//    }
//
//    public static class SimpleNested implements TestFilters.Nested {
//        @Nullable
//        private final String nestedString;
//
//        public SimpleNested(@Nullable String nestedString) {
//            this.nestedString = nestedString;
//        }
//
//        @javax.annotation.Nullable
//        @Nullable
//        public String getNestedString() {
//            return nestedString;
//        }
//    }
//}
