package params;

import edu.byu.uapi.spi.dictionary.TypeFailure;
import edu.byu.uapi.spi.dictionary.TypeDictionary;
import edu.byu.uapi.spi.functional.Failure;
import edu.byu.uapi.spi.functional.Success;
import edu.byu.uapi.spi.functional.SuccessOrFailure;
import edu.byu.uapi.spi.input.*;
import kotlin.jvm.JvmClassMappingKt;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AllParamsListParamReader implements ListParamReader<AllParams> {

    @NotNull
    public SuccessOrFailure<QueryParamReader<AllParams>, TypeFailure<?>> getReader(@NotNull TypeDictionary dictionary) {
        return null;
    }

    private static final SearchParamsMeta SEARCH_PARAMS_META;
    private static final SortParamsMeta SORT_PARAMS_META;

    private static final FilterParamsMeta FILTER_PARAMS_META;

    private static final String FILTER_PARAMS_$A_STRING$ = "a_string";
    private static final String FILTER_PARAMS_$AN_INT$ = "an_int";
    private static final String FILTER_PARAMS_$MULTIPLE$ = "multiple";
    private static final String FILTER_PARAMS_$NESTED__NESTED_STRING$ = "nested.nested_string";
    private static final List<String> FILTER_PARAMS_NESTED_NAMES_$NESTED$ = Collections.singletonList(FILTER_PARAMS_$NESTED__NESTED_STRING$);

    private static final List<String> FILTER_PARAMS_NAMES = Collections.unmodifiableList(Arrays.asList(FILTER_PARAMS_$A_STRING$, FILTER_PARAMS_$AN_INT$, FILTER_PARAMS_$MULTIPLE$, "nested.nested_string"));

    static {
        final Set<String> search_$context$_fields_$build = new LinkedHashSet<String>();
        search_$context$_fields_$build.add("field");
        search_$context$_fields_$build.add("another_field");

        final Set<String> search_$context$_fields = Collections.unmodifiableSet(search_$context$_fields_$build);

        final Set<String> search_$another_context$_fields = Collections.singleton("a_field");

        final Map<String, Set<String>> search_$build = new LinkedHashMap<String, Set<String>>();
        search_$build.put("context", search_$context$_fields);
        search_$build.put("another_context", search_$another_context$_fields);

        final Map<String, Set<String>> search = Collections.unmodifiableMap(search_$build);

        SEARCH_PARAMS_META = new SearchParamsMeta(search);

        final List<String> sort_fields = Collections.unmodifiableList(Arrays.asList("field", "a_field", "another_default_field"));
        final List<String> sort_defaults = Collections.unmodifiableList(Arrays.asList("field", "another_default_field"));


        SORT_PARAMS_META = new SortParamsMeta(sort_fields, sort_defaults);

        final FilterField filter_fields_$a_string$ = new FilterField(FILTER_PARAMS_$A_STRING$, false);
        final FilterField filter_fields_$an_int$ = new FilterField(FILTER_PARAMS_$AN_INT$, false);
        final FilterField filter_fields_$multiple$ = new FilterField(FILTER_PARAMS_$MULTIPLE$, false);
        final FilterField filter_fields_$nested__nested_string$ = new FilterField(FILTER_PARAMS_$NESTED__NESTED_STRING$, false);

        final List<FilterField> filter_fields = Collections.unmodifiableList(Arrays.asList(filter_fields_$a_string$, filter_fields_$an_int$, filter_fields_$multiple$, filter_fields_$nested__nested_string$));

        FILTER_PARAMS_META = new FilterParamsMeta(filter_fields);
    }

    @NotNull
    public ListParamsMeta getMeta(@NotNull TypeDictionary dictionary) {
        return new ListParamsMeta(
                SEARCH_PARAMS_META,
                FILTER_PARAMS_META,
                SORT_PARAMS_META
        );
    }


    public static class Reader implements QueryParamReader<AllParams> {
        @NotNull
        public SuccessOrFailure<AllParams, TypeFailure<?>> deserializeQueryParams(@NotNull Map<String, ? extends Set<String>> values) {
            final Set<String> keys = values.keySet();
            final TestFilters filters;
            if (hasAny(keys, FILTER_PARAMS_NAMES)) {
                final String aString;
                final Set<String> aString$raw = values.get(FILTER_PARAMS_$A_STRING$);
                if (aString$raw == null || aString$raw.isEmpty()) {
                    aString = null;
                } else if (aString$raw.size() > 1) {
                    //noinspection unchecked
                    return fail(TestFilters.class, "Parameter '" + FILTER_PARAMS_$A_STRING$ + "' has more than one value, but is not repeatable!");
                } else {
                    aString = aString$raw.iterator().next();
                }

                final Integer anInt;
                final Set<String> anInt$raw = values.get(FILTER_PARAMS_$AN_INT$);
                if (anInt$raw == null || anInt$raw.isEmpty()) {
                    anInt = null;
                } else if (anInt$raw.size() > 1) {
                    //noinspection unchecked
                    return fail(TestFilters.class, "Parameter '" + FILTER_PARAMS_$AN_INT$ + "' has more than one value, but is not repeatable!");
                } else {
                    try {
                        anInt = Integer.parseInt(anInt$raw.iterator().next());
                    } catch (NumberFormatException ex) {
                        //noinspection unchecked
                        return fail(TestFilters.class, "Value for '" + FILTER_PARAMS_$AN_INT$ + "' is not a valid integer.", ex);
                    }
                }

                final Collection<Integer> multiple;
                final Set<String> multiple$raw = values.get(FILTER_PARAMS_$MULTIPLE$);
                if (multiple$raw == null || multiple$raw.isEmpty()) {
                    multiple = Collections.emptySet();
                } else {
                    Collection<Integer> multiple$build = new HashSet<Integer>();
                    for (String multiple$each : multiple$raw) {
                        try {
                            multiple$build.add(Integer.parseInt(multiple$each));
                        } catch (NumberFormatException ex) {
                            //noinspection unchecked
                            return fail(TestFilters.class, "Value for '" + FILTER_PARAMS_$MULTIPLE$ + "' is not a valid integer.", ex);
                        }
                    }
                    multiple = Collections.unmodifiableCollection(multiple$build);
                }




                filters = new SimpleTestFilters(
                        aString,
                        anInt,
                        multiple,
                        null
                );
            } else {
                filters = null;
            }

            //noinspection unchecked
            return new Success<AllParams>(new SimpleAllParams(
                    filters, null, null
            ));
        }

        private static <T> Failure<TypeFailure<?>> fail(Class<T> type, String message) {
            return fail(type, message, null);
        }

        private static <T> Failure<TypeFailure<?>> fail(Class<T> type, String message, Throwable cause) {
            return new Failure<TypeFailure<?>>(
                    new TypeFailure(
                            JvmClassMappingKt.getKotlinClass(type),
                            message,
                            cause
                    )
            );
        }

        private static boolean hasAny(Set<String> toCheck, Collection<String> toFind) {
            for (String each : toFind) {
                if (toCheck.contains(each)) {
                    return true;
                }
            }
            return false;
        }

    }
}
