package params;

import edu.byu.uapi.spi.annotations.CollectionParams;
import edu.byu.uapi.spi.input.ListParams;

@CollectionParams
public interface AllParams extends ListParams.Filtering<TestFilters>,
        ListParams.Searching<TestSearchContext>,
        ListParams.Sorting<TestSortField>
{
}
