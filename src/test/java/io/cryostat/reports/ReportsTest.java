/*
 * Copyright The Cryostat Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cryostat.reports;

import static io.restassured.RestAssured.given;

import java.util.Map;

import io.cryostat.AbstractTransactionalTestBase;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(Reports.class)
public class ReportsTest extends AbstractTransactionalTestBase {

    @Test
    void testGetBadArchiveSource() {
        given().log()
                .all()
                .when()
                .get("/api/v4/reports/nonexistent")
                .then()
                .log()
                .all()
                .and()
                .assertThat()
                .statusCode(400);
    }

    @Test
    void testGetNonexistentTargetSource() {
        given().log()
                .all()
                .when()
                .pathParams("targetId", Integer.MAX_VALUE, "recordingName", "foo")
                .get("/api/v4/targets/{targetId}/reports/{recordingName}")
                .then()
                .log()
                .all()
                .and()
                .assertThat()
                .statusCode(404);
    }

    @Test
    void testGetNonexistentRecordingSource() {
        int targetId = defineSelfCustomTarget();
        given().log()
                .all()
                .when()
                .pathParams("targetId", targetId, "recordingName", "foo")
                .get("/api/v4/targets/{targetId}/reports/{recordingName}")
                .then()
                .log()
                .all()
                .and()
                .assertThat()
                .statusCode(404);
    }

    @Test
    void testGetReportByTargetAndRemoteId() {
        int targetId = defineSelfCustomTarget();
        int remoteId =
                given().log()
                        .all()
                        .when()
                        .pathParams(Map.of("targetId", targetId))
                        .formParam("recordingName", "activeRecordingsTest")
                        .formParam("events", "template=Continuous")
                        .pathParam("targetId", targetId)
                        .post("/api/v4/targets/{targetId}/recordings")
                        .then()
                        .log()
                        .all()
                        .and()
                        .assertThat()
                        .statusCode(201)
                        .and()
                        .extract()
                        .body()
                        .jsonPath()
                        .getInt("remoteId");

        given().log()
                .all()
                .when()
                .pathParams("targetId", targetId, "remoteId", remoteId)
                .get("/api/v4/targets/{targetId}/reports/{remoteId}")
                .then()
                .log()
                .all()
                .and()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", Matchers.greaterThan(0));

        given().log()
                .all()
                .when()
                .pathParams("targetId", targetId, "remoteId", remoteId)
                .delete("/api/v4/targets/{targetId}/recordings/{remoteId}")
                .then()
                .log()
                .all()
                .and()
                .assertThat()
                .statusCode(204);
    }

    @Test
    void testGetReportByUrl() {
        int targetId = defineSelfCustomTarget();
        JsonPath recording =
                given().log()
                        .all()
                        .when()
                        .pathParams(Map.of("targetId", targetId))
                        .formParam("recordingName", "activeRecordingsTest")
                        .formParam("events", "template=Continuous")
                        .pathParam("targetId", targetId)
                        .post("/api/v4/targets/{targetId}/recordings")
                        .then()
                        .log()
                        .all()
                        .and()
                        .assertThat()
                        .statusCode(201)
                        .and()
                        .extract()
                        .body()
                        .jsonPath();

        String reportUrl = recording.getString("reportUrl");
        int remoteId = recording.getInt("remoteId");

        given().log()
                .all()
                .when()
                .get(reportUrl)
                .then()
                .log()
                .all()
                .and()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", Matchers.greaterThan(0));

        given().log()
                .all()
                .when()
                .pathParams("targetId", targetId, "remoteId", remoteId)
                .delete("/api/v4/targets/{targetId}/recordings/{remoteId}")
                .then()
                .log()
                .all()
                .and()
                .assertThat()
                .statusCode(204);
    }
}
