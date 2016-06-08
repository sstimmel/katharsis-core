package io.katharsis.itests

import io.katharsis.request.Request
import io.katharsis.request.path.JsonApiPath
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@RunWith(value = SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = arrayOf(IntegrationConfig::class))
class KatharsisRoutingTest() : KatharsisIntegrationSupport() {

    @Test
    fun testGetCollectionReturnsEmptyResponse() {
        val path = JsonApiPath.parsePathFromStringUrl("http://domain/tasks")
        val req = Request(path, "GET", null, paramProvider)

        var res = requestDispatcher.dispatchRequest(req)

        assertNotNull(res)
        assertEquals(200, res.httpStatus)
    }
}