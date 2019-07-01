package params;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public interface TestFilters {
    @Nullable
    String getAString();

    @Nullable
    Integer getAnInt();

    @Nonnull
    Collection<Integer> getMultiple();

    @Nullable
    Nested getNested();

    public interface Nested {
        @Nullable
        String getNestedString();
    }
}

