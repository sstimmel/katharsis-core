package io.katharsis.dispatcher.controller.collection;

import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.request.Request;
import io.katharsis.response.BaseResponseContext;
import org.junit.Assert;
import org.junit.Test;

import static io.katharsis.request.path.JsonApiPath.parsePathFromStringUrl;

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

//    @Test
//    public void onGivenRequestResourceWithIdShouldSetIt() throws Exception {
//        // GIVEN
//        RequestBody requestBody = new RequestBody();
//        DataBody data = new DataBody();
//        requestBody.setData(data);
//        long taskId = Long.MAX_VALUE - 1L;
//        data.setType("tasks");
//        data.setId(Long.toString(taskId));
//
//        JsonPath taskPath = pathBuilder.buildPath("/tasks");
//        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper, queryParamsBuilder);
//
//        // WHEN -- adding a task
//        BaseResponseContext taskResponse = resourcePost.handle(taskPath, new QueryParams(), requestBody);
//
//        // THEN
//        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
//        Long persistedTaskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
//        assertThat(persistedTaskId).isEqualTo(taskId);
//    }
//
//    @Test
//    public void onGivenRequestResourceShouldLoadAutoIncludeFields() throws Exception {
//        // GIVEN
//        RequestBody newTaskBody = new RequestBody();
//        DataBody data = new DataBody();
//        newTaskBody.setData(data);
//        data.setType("tasks");
//        data.setAttributes(objectMapper.createObjectNode().put("name", "sample task"));
//        data.setRelationships(new ResourceRelationships());
//
//        JsonPath taskPath = pathBuilder.buildPath("/tasks");
//        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper, queryParamsBuilder);
//
//        // WHEN -- adding a task
//        BaseResponseContext taskResponse = resourcePost.handle(taskPath, new QueryParams(), newTaskBody);
//
//        // THEN
//        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
//        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
//        assertThat(taskId).isNotNull();
//
//        /* ------- */
//
//        // GIVEN
//        RequestBody newProjectBody = new RequestBody();
//        data = new DataBody();
//        newProjectBody.setData(data);
//        data.setType("projects");
//        data.setAttributes(objectMapper.createObjectNode().put("name", "sample project"));
//
//        JsonPath projectPath = pathBuilder.buildPath("/projects");
//
//        // WHEN -- adding a project
//        ResourceResponseContext projectResponse = resourcePost.handle(projectPath, new QueryParams(), newProjectBody);
//
//        // THEN
//        assertThat(projectResponse.getResponse().getEntity()).isExactlyInstanceOf(Project.class);
//        assertThat(((Project) (projectResponse.getResponse().getEntity())).getId()).isNotNull();
//        assertThat(((Project) (projectResponse.getResponse().getEntity())).getName()).isEqualTo("sample project");
//        Long projectId = ((Project) (projectResponse.getResponse().getEntity())).getId();
//        assertThat(projectId).isNotNull();
//
//        /* ------- */
//
//        // GIVEN
//        RequestBody newTaskToProjectBody = new RequestBody();
//        data = new DataBody();
//        newTaskToProjectBody.setData(Collections.singletonList(data));
//        data.setType("projects");
//        data.setId(projectId.toString());
//
//        JsonPath savedTaskPath = pathBuilder.buildPath("/tasks/" + taskId + "/relationships/includedProjects");
//        RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser, queryParamsBuilder);
//
//        // WHEN -- adding a relation between task and project
//        BaseResponseContext projectRelationshipResponse = sut.handle(savedTaskPath, new QueryParams(),
//                newTaskToProjectBody);
//        assertThat(projectRelationshipResponse).isNotNull();
//
//        // THEN
//        TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
//        Project project = taskToProjectRepository.findOneTarget(taskId, "includedProjects", REQUEST_PARAMS);
//        assertThat(project.getId()).isEqualTo(projectId);
//
//        //Given
//        JsonPath jsonPath = pathBuilder.buildPath("/tasks/" + taskId);
//        ResourceGet responseGetResp = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter, queryParamsBuilder);
//        Map<String, Set<String>> queryParams = new HashMap<>();
//        queryParams.put(RestrictedQueryParamsMembers.include.name() + "[tasks]",
//                Collections.singleton("includedProjects"));
//        QueryParams queryParams1 = new QueryParamsBuilder(new DefaultQueryParamsParser()).buildQueryParams(queryParams);
//
//        // WHEN
//        BaseResponseContext response = responseGetResp.handle(jsonPath, queryParams1, null);
//
//        // THEN
//        Assert.assertNotNull(response);
//        assertThat(response.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
//        assertThat(((Task) (taskResponse.getResponse().getEntity())).getIncludedProjects()).isNotNull();
//        assertThat(((Task) (taskResponse.getResponse().getEntity())).getIncludedProjects().size()).isEqualTo(1);
//        assertThat(((Task) (taskResponse.getResponse().getEntity())).getIncludedProjects().get(0).getId()).isEqualTo(projectId);
//    }
//
//    @Test
//    public void onGivenRequestResourceShouldNotLoadAutoIncludeFields() throws Exception {
//        // GIVEN
//        RequestBody newTaskBody = new RequestBody();
//        DataBody data = new DataBody();
//        newTaskBody.setData(data);
//        data.setType("tasks");
//        data.setAttributes(objectMapper.createObjectNode().put("name", "sample task"));
//        data.setRelationships(new ResourceRelationships());
//
//        JsonPath taskPath = pathBuilder.buildPath("/tasks");
//        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper, queryParamsBuilder);
//
//        // WHEN -- adding a task
//        BaseResponseContext taskResponse = resourcePost.handle(taskPath, new QueryParams(), newTaskBody);
//
//        // THEN
//        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
//        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
//        assertThat(taskId).isNotNull();
//
//        /* ------- */
//
//        // GIVEN
//        RequestBody newProjectBody = new RequestBody();
//        data = new DataBody();
//        newProjectBody.setData(data);
//        data.setType("projects");
//        data.setAttributes(objectMapper.createObjectNode().put("name", "sample project"));
//
//        JsonPath projectPath = pathBuilder.buildPath("/projects");
//
//        // WHEN -- adding a project
//        ResourceResponseContext projectResponse = resourcePost.handle(projectPath, new QueryParams(), newProjectBody);
//
//        // THEN
//        assertThat(projectResponse.getResponse().getEntity()).isExactlyInstanceOf(Project.class);
//        assertThat(((Project) (projectResponse.getResponse().getEntity())).getId()).isNotNull();
//        assertThat(((Project) (projectResponse.getResponse().getEntity())).getName()).isEqualTo("sample project");
//        Long projectId = ((Project) (projectResponse.getResponse().getEntity())).getId();
//        assertThat(projectId).isNotNull();
//
//        /* ------- */
//
//        // GIVEN
//        RequestBody newTaskToProjectBody = new RequestBody();
//        data = new DataBody();
//        newTaskToProjectBody.setData(Collections.singletonList(data));
//        data.setType("projects");
//        data.setId(projectId.toString());
//
//        JsonPath savedTaskPath = pathBuilder.buildPath("/tasks/" + taskId + "/relationships/projects");
//        RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser, queryParamsBuilder);
//
//        // WHEN -- adding a relation between task and project
//        BaseResponseContext projectRelationshipResponse = sut.handle(savedTaskPath, new QueryParams(), newTaskToProjectBody);
//        assertThat(projectRelationshipResponse).isNotNull();
//
//        // THEN
//        TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
//        Project project = taskToProjectRepository.findOneTarget(taskId, "projects", REQUEST_PARAMS);
//        assertThat(project.getId()).isNotNull();
//
//        //Given
//        JsonPath jsonPath = pathBuilder.buildPath("/tasks/" + taskId);
//        ResourceGet responseGetResp = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter, queryParamsBuilder);
//        Map<String, Set<String>> queryParams = new HashMap<>();
//        queryParams.put(RestrictedQueryParamsMembers.include.name() + "[tasks]",
//                Collections.singleton("[projects]"));
//        QueryParams requestParams = new QueryParamsBuilder(new DefaultQueryParamsParser()).buildQueryParams(queryParams);
//
//        // WHEN
//        BaseResponseContext response = responseGetResp.handle(jsonPath, requestParams, null);
//
//        // THEN
//        Assert.assertNotNull(response);
//        assertThat(response.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
//        assertThat(((Task) (taskResponse.getResponse().getEntity())).getProjects()).isNull();
//    }
}
