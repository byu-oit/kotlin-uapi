package params;

import edu.byu.uapi.spi.annotations.DefaultSort;

public enum TestSortField {
    @DefaultSort(order = 1)
    field,
    a_field,
    @DefaultSort(order = 2)
    another_default_field
}
