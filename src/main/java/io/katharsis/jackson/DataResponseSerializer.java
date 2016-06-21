package io.katharsis.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.katharsis.domain.api.DataResponse;
import io.katharsis.jackson.exception.JsonSerializationException;
import io.katharsis.jackson.serializer.KatharsisFieldPropertyFilter;
import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiLinksInformation;
import io.katharsis.resource.annotations.JsonApiMetaInformation;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;
import io.katharsis.utils.Predicate2;
import io.katharsis.utils.PropertyUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DataResponseSerializer extends JsonSerializer<DataResponse> {

    private static final String JACKSON_ATTRIBUTE_FILTER_NAME = "katharsisFilter";

    @Override
    public void serialize(DataResponse value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException, JsonProcessingException {

        if (value == null && value.getData() == null) {
            gen.writeObject(null);
            return;
        }

        gen.writeStartObject();

        if (value.getJsonApi() != null) {
            gen.writeObjectField("jsonapi", value.getJsonApi());
        }
        if (value.getData() != null) {
            gen.writeFieldName("data");

            if (value.getData() instanceof Collection) {
                Collection data = value.getData();
                writeCollectionResource(gen, data);
            } else {
                writeSingleResource(gen, value.getData());
            }
        }

        if (value.getMeta() != null) {
            gen.writeObjectField("meta", value.getMeta());
        }

        if (value.getLinks() != null) {
            gen.writeObjectField("links", value.getLinks());
        }

        if (value.getIncluded() != null) {
            gen.writeObjectField("included", value.getIncluded());
        }

        gen.writeEndObject();
    }

    private void writeSingleResource(JsonGenerator gen, Object resource) throws IOException {
        gen.writeStartObject();

        gen.writeObjectField("type", resourceName(resource));
        writeIdAndAttributesAndRelationships(gen, resource);

        gen.writeEndObject();
    }

    private void writeIdAndAttributesAndRelationships(JsonGenerator gen, Object resource) throws IOException {
        final Map<String, Object> attributes = new HashMap<>();
        final Map<String, Object> relationships = new HashMap<>();

        //TODO: ieugen: we can use APT to generate a static transformation for files instead of using reflections
        // http://hannesdorfmann.com/annotation-processing/annotationprocessing101

        for (Field field : resource.getClass().getDeclaredFields()) {
            JsonApiId jsonApiId = field.getAnnotation(JsonApiId.class);

            JsonApiMetaInformation meta = field.getAnnotation(JsonApiMetaInformation.class);
            JsonApiLinksInformation links = field.getAnnotation(JsonApiLinksInformation.class);

            JsonApiToOne toOne = field.getAnnotation(JsonApiToOne.class);
            JsonApiToMany toMany = field.getAnnotation(JsonApiToMany.class);

            if (jsonApiId != null) {
                gen.writeStringField("id", String.valueOf(PropertyUtils.getProperty(resource, field.getName())));
            } else if (meta != null) {
                //TODO: ieugen meta should be processed at this point as they are outside 'data:{}'
            } else if (links != null) {
                //TODO: ieugen links should be processed at this point as they are outside 'data:{}'
            } else if (toOne != null || toMany != null) {
                relationships.put(field.getName(), PropertyUtils.getProperty(resource, field.getName()));
            } else {
                attributes.put(field.getName(), PropertyUtils.getProperty(resource, field.getName()));
            }
        }

        writeAttributes(gen, attributes);
        writeRelationships(gen, relationships);
    }

    private void writeAttributes(JsonGenerator gen, Map<String, Object> attributes) throws IOException {
        gen.writeFieldName("attributes");
        gen.writeStartObject();
        // write attributes map
        for (Map.Entry<String, Object> attr : attributes.entrySet()) {
            gen.writeObjectField(attr.getKey(), attr.getValue());
        }
        gen.writeEndObject();
    }

    private void writeRelationships(JsonGenerator gen, @NonNull Map<String, Object> relationships) throws IOException {
        if (!relationships.isEmpty()) {
            gen.writeFieldName("relationships");
            gen.writeStartObject();

            for (Map.Entry<String, Object> relationship : relationships.entrySet()) {
                gen.writeFieldName(relationship.getKey());
//                gen.writeStartObject();
                gen.writeObject(relationship.getValue());
//                gen.writeEndObject();
            }

            gen.writeEndObject();
        }
    }

    private String resourceName(Object data) {
        JsonApiResource resource = data.getClass().getAnnotation(JsonApiResource.class);
        if (resource == null) {
            throw new JsonSerializationException("Missing JsonApiResource annotation.");
        }

        return resource.type();
    }

    /**
     * Generate a new object mapper and configure the filter to exclude some properties.
     */
    protected ObjectMapper katharsisAttributeObjectMapper(JsonGenerator gen,
                                                          final Object data,
                                                          Predicate2<Object, PropertyWriter> includedFields) {

        FilterProvider fp = new SimpleFilterProvider()
                .addFilter(JACKSON_ATTRIBUTE_FILTER_NAME, new KatharsisFieldPropertyFilter(includedFields));

        ObjectMapper attributesObjectMapper = ((ObjectMapper) gen.getCodec()).copy();
        attributesObjectMapper.setFilterProvider(fp);
        attributesObjectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            public Object findFilterId(Annotated a) {
                Object filterId = null;

                if (a instanceof AnnotatedClass) {
                    AnnotatedClass ac = (AnnotatedClass) a;
                    if (ac.getRawType().equals(data.getClass())) {
                        filterId = JACKSON_ATTRIBUTE_FILTER_NAME;
                    }
                }
                return filterId;
            }
        });

        return attributesObjectMapper;
    }

    private void writeCollectionResource(JsonGenerator gen, Collection data) throws IOException {

        gen.writeStartArray();
        for (Object element : data) {
            writeSingleResource(gen, element);
        }
        gen.writeEndArray();
    }

    @Override
    public Class<DataResponse> handledType() {
        return DataResponse.class;
    }
}
