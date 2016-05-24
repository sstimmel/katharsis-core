package io.katharsis.dispatcher.controller.collection;

import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.dispatcher.controller.resource.RelationshipsResourcePost;
import io.katharsis.dispatcher.controller.resource.ResourceGet;
import io.katharsis.dispatcher.controller.resource.ResourcePost;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.Request;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.dto.ResourceRelationships;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.RestrictedQueryParamsMembers;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.response.BaseResponseContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.katharsis.request.path.JsonApiPath.parsePathFromStringUrl;
import static org.assertj.core.api.Assertions.assertThat;

public class CollectionGetTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = "GET";

    @Test
    public void onGivenRequestCollectionGetShouldAcceptIt() {
        // GIVEN
        Request request = new Request(parsePathFromStringUrl("http://domain.local/tasks/"), REQUEST_TYPE, null, parameterProvider);

        CollectionGet sut = new CollectionGet(resourceRegistry, typeParser, includeFieldSetter, queryParamsBuilder);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        Assert.assertEquals(result, true);
    }

    @Test
    public void onGivenRequestCollectionGetShouldDenyIt() {
        // GIVEN
        Request request = new Request(parsePathFromStringUrl("http://domain.local/tasks/2"), REQUEST_TYPE, null, parameterProvider);

        CollectionGet sut = new CollectionGet(resourceRegistry, typeParser, includeFieldSetter, queryParamsBuilder);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        Assert.assertEquals(result, false);
    }

    @Test
    public void onGivenRequestCollectionGetShouldHandleIt() {
        // GIVEN

        Request request = new Request(parsePathFromStringUrl("http://domain.local/tasks/"), REQUEST_TYPE, null, parameterProvider);
        CollectionGet sut = new CollectionGet(resourceRegistry, typeParser, includeFieldSetter, queryParamsBuilder);

        // WHEN
        BaseResponseContext response = sut.handle(request);

        // THEN
        Assert.assertNotNull(response);
    }

    @Test
    public void onGivenRequestCollectionWithIdsGetShouldHandleIt() {
        // GIVEN
        Request request = new Request(parsePathFromStringUrl("http://domain.local/tasks/1,2"), REQUEST_TYPE, null, parameterProvider);

        CollectionGet sut = new CollectionGet(resourceRegistry, typeParser, includeFieldSetter, queryParamsBuilder);

        // WHEN
        BaseResponseContext response = sut.handle(request);

        // THEN
        Assert.assertNotNull(response);
    }

    @Test
    public void onGivenRequestResourceWithIdShouldSetIt() throws Exception {
        // GIVEN
        long taskId = Long.MAX_VALUE - 1L;
        RequestBody requestBody = new RequestBody(DataBody.builder()
                .type("tasks")
                .id(Long.toString(taskId))
                .build());

        JsonApiPath taskPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(taskPath, REQUEST_TYPE, serialize(requestBody), parameterProvider);

        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper, queryParamsBuilder);

        // WHEN -- adding a task
        BaseResponseContext taskResponse = resourcePost.handle(request);

        // THEN
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long persistedTaskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(persistedTaskId).isEqualTo(taskId);
    }

    @Test
    public void onGivenRequestResourceShouldLoadAutoIncludeFields() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody(DataBody.builder()
                .type("tasks")
                .attributes(objectMapper.createObjectNode().put("name", "sample task"))
                .relationships(new ResourceRelationships())
                .build());

        JsonApiPath taskPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(taskPath, REQUEST_TYPE, serialize(newTaskBody), parameterProvider);

        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper, queryParamsBuilder);

        // WHEN -- adding a task
        BaseResponseContext taskResponse = resourcePost.handle(request);

        // THEN
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newProjectBody = new RequestBody(DataBody.builder()
                .type("projects")
                .attributes(objectMapper.createObjectNode().put("name", "sample project"))
                .build());

        JsonApiPath projectPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/projects");
        request = new Request(projectPath, REQUEST_TYPE, serialize(newProjectBody), parameterProvider);

        // WHEN -- adding a project
        BaseResponseContext projectResponse = resourcePost.handle(request);

        // THEN
        assertThat(projectResponse.getResponse().getEntity()).isExactlyInstanceOf(Project.class);
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getId()).isNotNull();
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getName()).isEqualTo("sample project");
        Long projectId = ((Project) (projectResponse.getResponse().getEntity())).getId();
        assertThat(projectId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newTaskToProjectBody = new RequestBody(Collections.singletonList(DataBody.builder()
                .type("projects")
                .id(projectId.toString())
                .build()));

        JsonApiPath savedTaskPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/" + taskId + "/relationships/includedProjects");
        request = new Request(savedTaskPath, REQUEST_TYPE, serialize(newTaskToProjectBody), parameterProvider);

        RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser, queryParamsBuilder);

        // WHEN -- adding a relation between task and project
        BaseResponseContext projectRelationshipResponse = sut.handle(request);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
        Project project = taskToProjectRepository.findOneTarget(taskId, "includedProjects", REQUEST_PARAMS);
        assertThat(project.getId()).isEqualTo(projectId);

        //Given
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/" + taskId);
        request = new Request(jsonPath, REQUEST_TYPE, serialize(newTaskToProjectBody), parameterProvider);

        ResourceGet responseGetResp = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter, queryParamsBuilder);
        Map<String, Set<String>> queryParams = new HashMap<>();
        queryParams.put(RestrictedQueryParamsMembers.include.name() + "[tasks]",
                Collections.singleton("includedProjects"));

        QueryParams queryParams1 = queryParamsBuilder.buildQueryParams(queryParams);

        // WHEN
        BaseResponseContext response = responseGetResp.handle(request);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        assertThat(((Task) (taskResponse.getResponse().getEntity())).getIncludedProjects()).isNotNull();
        assertThat(((Task) (taskResponse.getResponse().getEntity())).getIncludedProjects().size()).isEqualTo(1);
        assertThat(((Task) (taskResponse.getResponse().getEntity())).getIncludedProjects().get(0).getId()).isEqualTo(projectId);
    }

    @Test
    public void onGivenRequestResourceShouldNotLoadAutoIncludeFields() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody(DataBody.builder()
                .type("tasks")
                .attributes(objectMapper.createObjectNode().put("name", "sample task"))
                .relationships(new ResourceRelationships())
                .build());

        JsonApiPath taskPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(taskPath, REQUEST_TYPE, serialize(newTaskBody), parameterProvider);

        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper, queryParamsBuilder);

        // WHEN -- adding a task
        BaseResponseContext taskResponse = resourcePost.handle(request);

        // THEN
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newProjectBody = new RequestBody(DataBody.builder()
                .type("projects")
                .attributes(objectMapper.createObjectNode().put("name", "sample project"))
                .build());

        JsonApiPath projectPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/projects");
        request = new Request(projectPath, REQUEST_TYPE, serialize(newProjectBody), parameterProvider);
        // WHEN -- adding a project
        BaseResponseContext projectResponse = resourcePost.handle(request);

        // THEN
        assertThat(projectResponse.getResponse().getEntity()).isExactlyInstanceOf(Project.class);
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getId()).isNotNull();
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getName()).isEqualTo("sample project");
        Long projectId = ((Project) (projectResponse.getResponse().getEntity())).getId();
        assertThat(projectId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newTaskToProjectBody = new RequestBody(Collections.singletonList(DataBody.builder()
                .id(projectId.toString())
                .type("projects")
                .build()));

        JsonApiPath savedTaskPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/" + taskId + "/relationships/projects");
        request = new Request(savedTaskPath, REQUEST_TYPE, serialize(newTaskToProjectBody), parameterProvider);
        RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser, queryParamsBuilder);

        // WHEN -- adding a relation between task and project
        BaseResponseContext projectRelationshipResponse = sut.handle(request);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
        Project project = taskToProjectRepository.findOneTarget(taskId, "projects", REQUEST_PARAMS);
        assertThat(project.getId()).isNotNull();

        //Given
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/" + taskId);
        request = new Request(jsonPath, REQUEST_TYPE, serialize(newTaskToProjectBody), parameterProvider);
        ResourceGet responseGetResp = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter, queryParamsBuilder);
        Map<String, Set<String>> queryParams = new HashMap<>();
        queryParams.put(RestrictedQueryParamsMembers.include.name() + "[tasks]",
                Collections.singleton("[projects]"));
        QueryParams requestParams = queryParamsBuilder.buildQueryParams(queryParams);

        // WHEN
        BaseResponseContext response = responseGetResp.handle(request);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        assertThat(((Task) (taskResponse.getResponse().getEntity())).getProjects()).isNull();
    }
}
