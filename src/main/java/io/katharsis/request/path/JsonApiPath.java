package io.katharsis.request.path;


import io.katharsis.utils.java.Optional;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A JSON API Path has between 1-4 elements
 * <p>
 * / resource / id(s) / field | "relationships" / relationship
 * <p>
 * This class parses a Path and identifies all parts.
 */
public class JsonApiPath {

    public static final String DEFAULT_ID_SEPARATOR = ",";
    public static final String SEPARATOR = "/";
    public static final String RELATIONSHIP_MARK = "relationships";

    private String resource;
    private Optional<Set<String>> ids;
    private Optional<String> relationship;
    private Optional<String> field;

    private JsonApiPath(String resource, Set<String> ids, String relationship, String field) {
        this(resource, Optional.ofNullable(ids), Optional.ofNullable(relationship), Optional.ofNullable(field));
    }

    private JsonApiPath(String resource,
                        Optional<Set<String>> ids,
                        Optional<String> relationship,
                        Optional<String> field) {
        this.resource = resource;
        this.ids = ids;
        this.relationship = relationship;
        this.field = field;
    }

    private static String[] splitPath(String path) {
        if (path.startsWith(SEPARATOR)) {
            path = path.substring(1);
        }
        if (path.endsWith(SEPARATOR)) {
            path = path.substring(0, path.length());
        }
        return path.split(SEPARATOR);
    }

    /**
     * Parses path provided by the application. The path provided cannot contain neither hostname nor protocol. It
     * can start or end with slash e.g. <i>/tasks/1/</i> or <i>tasks/1</i>.
     *
     * @param path Path to be parsed
     * @return doubly-linked list which represents path given at the input
     */
    public static JsonApiPath parsePath(URL path) {
        String[] pathParts = splitPath(path.getPath());

        validatePath(pathParts);

        String resource = parseResource(pathParts);

        Optional<Set<String>> ids = parseIds(pathParts);
        Optional<String> relationship = relationship(pathParts);
        Optional<String> field = parseField(pathParts);

        return new JsonApiPath(resource, ids, relationship, field);
    }

    private static void validatePath(String[] pathParts) {
        if (pathParts.length == 0) {
            throw new IllegalStateException("Path must have at leas one element");
        }

        if (pathParts.length > 4) {
            throw new IllegalStateException("Path has too many elements " + pathParts);
        }

        if (pathParts.length == 4) {
            if (!pathParts[2].equalsIgnoreCase(RELATIONSHIP_MARK)) {
                throw new IllegalStateException("No relationships mark was not found at position 3 " + pathParts);
            }
        }
    }

    private static String parseResource(String[] strings) {
        return strings[0];
    }

    private static Optional<Set<String>> parseIds(String[] parts) {
        if (!hasIds(parts)) {
            return Optional.empty();
        }
        String[] idsStrings = parts[1].split(DEFAULT_ID_SEPARATOR);

        Set<String> ids = new LinkedHashSet<>();
        for (String id : idsStrings) {
            ids.add(id);
        }
        return Optional.of(ids);
    }

    private static Optional<String> parseField(String[] strings) {
        if (!hasField(strings)) {
            return Optional.empty();
        }
        return Optional.of(strings[2]);
    }

    private static Optional<String> relationship(String[] strings) {
        if (!hasRelationship(strings)) {
            return Optional.empty();
        }
        return Optional.of(strings[3]);
    }

    protected static boolean isCollection(String[] strings) {
        return (strings.length == 1) || (strings.length == 2 && strings[1].contains(DEFAULT_ID_SEPARATOR));
    }

    protected static boolean hasIds(String[] parts) {
        return parts.length >= 2;
    }

    protected static boolean hasField(String[] parts) {
        return parts.length == 3;
    }

    protected static boolean hasRelationship(String[] parts) {
        // if we have 4 elements, check for relationships mark
        return parts.length == 4 ? parts[2].equalsIgnoreCase(RELATIONSHIP_MARK) : false;
    }

    public boolean isCollection() {
        if (ids.isPresent()) {
            return ids.get().size() > 1;
        } else {
            // resource is always present, check for absence of field and relationship
            return !(field.isPresent() || relationship.isPresent());
        }
    }

    public String getResource() {
        return resource;
    }

    public Optional<Set<String>> getIds() {
        return ids;
    }

    public Optional<String> getRelationship() {
        return relationship;
    }

    public Optional<String> getField() {
        return field;
    }
}
