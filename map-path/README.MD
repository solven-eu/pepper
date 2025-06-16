This inspires from JsonPath to manipulate Maps. Typically, it can be used to transform a `java.util.Map` into a flat format, where entry entry is a jsonPath:

# Specification

We try to follow https://datatracker.ietf.org/doc/rfc9535/

# Examples

Given `$.some.path`, `MapPath.split(String)` will return a `List<Object>` where each element is a step down.

Given some `Map`, `MapPathGet.getOptionalAs(map, path)` will drill-down `map` and return the found value as `Optional`.

For instance, `MapPathGet.getOptionalAs(Map.of("foo", Map.of("bar", LocalDate.now()), List.of("foo", "bar"))` would return `LocalDate.now()`.


# Arrays

`MapPathGet` is typically used over a recursive `Map`. It also work if some value (including the root) is a `List`. In such a case, the path component is expected to be an `int`:

For instance, `MapPathGet.getOptionalString(Map.of("some", List.of("foo", "bar")), "some", 1)` would return `"bar"`.

    

## Type-specific

The getter methods have `String` and other specialization:

    Optional<String> optString = MapPathGet.getOptionalString(map, "some", path)