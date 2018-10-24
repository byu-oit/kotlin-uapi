package params;

import edu.byu.uapi.spi.annotations.SearchFields;

public enum TestSearchContext {
    @SearchFields({"field", "another_field"})
    context,
    @SearchFields({"a_field"})
    another_context
}
