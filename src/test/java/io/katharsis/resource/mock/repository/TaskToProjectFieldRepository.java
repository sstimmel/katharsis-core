package io.katharsis.resource.mock.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.FieldRepository;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.util.Relation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class TaskToProjectFieldRepository implements FieldRepository<Task, Long, Project, Long> {

    public static final ConcurrentMap<Relation<Long>, Integer> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();
    private static final AtomicLong ATOMIC_LONG = new AtomicLong(1);

    public void removeRelations(String fieldName) {
        Iterator<Relation<Long>> iterator = THREAD_LOCAL_REPOSITORY.keySet().iterator();
        while (iterator.hasNext()) {
            Relation<Long> next = iterator.next();
            if (next.getFieldName().equals(fieldName)) {
                iterator.remove();
            }
        }
    }


    @Override
    public Project addField(Long resourceId, Project field, String fieldName, QueryParams queryParams) {
        removeRelations(fieldName);
        field.setId(ATOMIC_LONG.getAndIncrement());
        if (resourceId != null) {
            THREAD_LOCAL_REPOSITORY.put(new Relation<>(resourceId, field.getId(), fieldName), 0);
        }

        return field;
    }

    @Override
    public Iterable<Project> addFields(Long resourceId, Iterable<Project> fields, String fieldName, QueryParams queryParams) {
        fields.forEach(field -> {
            field.setId(ATOMIC_LONG.getAndIncrement());
            THREAD_LOCAL_REPOSITORY.put(new Relation<>(resourceId, field.getId(), fieldName), 0);
            }
        );

        return fields;
    }

    @Override
    public void deleteField(Long resourceId, String fieldName, QueryParams queryParams) {
        removeRelations(fieldName);

    }

    @Override
    public void deleteFields(Long resourceId, Iterable<Long> targetIds, String fieldName, QueryParams queryParams) {
        targetIds.forEach(targetId -> {
            Iterator<Relation<Long>> iterator = THREAD_LOCAL_REPOSITORY.keySet().iterator();
            while (iterator.hasNext()) {
                Relation<Long> next = iterator.next();
                if (next.getFieldName().equals(fieldName) && next.getTargetId().equals(targetId)) {
                    iterator.remove();
                }
            }
        });
    }
}
