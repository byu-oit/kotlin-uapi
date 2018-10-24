package params;

import edu.byu.uapi.spi.annotations.CollectionParams;
import edu.byu.uapi.spi.input.Params;

@CollectionParams
public interface AllParams extends Params.Filtering<TestFilters>,
        Params.Searching<TestSearchContext>,
        Params.Sorting<TestSortField>
{
}
