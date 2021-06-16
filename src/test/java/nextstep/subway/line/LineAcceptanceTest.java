package nextstep.subway.line;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nextstep.subway.utils.RestAssuredCRUD.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {

    private String path = "/lines";

    private LineRequest line2Request;
    private LineRequest line6Request;

    public static ExtractableResponse<Response> 지하철_노선_등록되어_있음(Map<String, String> createParams) {
        return postRequest("/lines", createParams);
    }

    @BeforeEach
    void setup() {
        line2Request = 라인2를_생성하기위한_request를_만든다();
        line6Request = 라인6을_생성하기위한_request를_만든다();
    }

    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createLine() {
        // given
        LineRequest lineRequest = line2Request;

        // when
        // 지하철_노선_생성_요청
        ExtractableResponse<Response> response = postRequest(path, lineRequest);

        // then
        // 지하철_노선_생성됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();

        // and
        // Check stations
        LineResponse lineResponse = response.as(LineResponse.class);
        List<StationResponse> stations = lineResponse.getStations();
        List<String> names = getNamesFromStations(stations);
        assertThat(names).containsExactly("강남역", "역삼역");
    }

    @DisplayName("기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성한다.")
    @Test
    void createLine2() {
        // given
        // 지하철_노선_등록되어_있음
        postRequest(path, line2Request);

        // when
        // 지하철_노선_생성_요청
        ExtractableResponse<Response> response = postRequest(path, line2Request);

        // then
        // 지하철_노선_생성_실패됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("지하철 노선 목록(모든 노선)을 조회한다.")
    @Test
    void getLines() {
        // given
        // 지하철_노선_등록되어_있음
        ExtractableResponse createResponse1 = postRequest(path, line2Request);
        // 지하철_노선_등록되어_있음
        ExtractableResponse createResponse2 = postRequest(path, line6Request);

        // when
        // 지하철_노선_목록_조회_요청
        ExtractableResponse<Response> response = get(path);

        // then
        // 지하철_노선_목록_응답됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        // 지하철_노선_목록_포함됨
        List<Long> expectedLineIds = Arrays.asList(createResponse1, createResponse2).stream()
                .map(it -> Long.parseLong(it.header("Location").split("/")[2]))
                .collect(Collectors.toList());
        List<Long> resultLineIds = response.jsonPath().getList(".", LineResponse.class).stream()
                .map(it -> it.getId())
                .collect(Collectors.toList());
        assertThat(resultLineIds).containsAll(expectedLineIds);
    }

    @DisplayName("지하철 노선(특정 1개)을 조회한다.")
    @Test
    void getLine() {
        // given
        // 지하철_노선_등록되어_있음 + 노선 안에 구간 1개(즉, 역 2개) 들어있음
        ExtractableResponse createResponse = postRequest(path, line2Request);

        // when
        // 지하철_노선_조회_요청
        String uri = createResponse.header("Location");
        ExtractableResponse<Response> response = get(uri);

        // then
        // 지하철_노선_응답됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        // 지하철 노선에 들어있는 역들을 상행종점부터 하행종점까지 반환한다.
        List<StationResponse> stationResponses = response.body().as(LineResponse.class).getStations();
        List<String> resultStationNames = getNamesFromStations(stationResponses);
        assertThat(resultStationNames).containsExactly("강남역", "역삼역");
    }

    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateLine() {
        // 현재는 통과하지만, 다른 방식의 update로 수정될 것 같음

        // given
        // 지하철_노선_등록되어_있음
        ExtractableResponse createResponse = postRequest(path, line2Request);

        // when
        // 지하철_노선_수정_요청
        String uri = createResponse.header("Location");
        ExtractableResponse createResponse2 = putRequest(uri, line6Request);

        // then
        // 지하철_노선_수정됨
        assertThat(createResponse2.statusCode()).isEqualTo(HttpStatus.OK.value());

        // and
        // 지하철_노선_수정확인
        LineResponse lineResponse = createResponse2.body().as(LineResponse.class);
        assertThat(lineResponse.getName()).isEqualTo(line6Request.getName());
        assertThat(lineResponse.getColor()).isEqualTo(line6Request.getColor());
    }

    @DisplayName("지하철 노선을 제거한다.")
    @Test
    void deleteLine() {
        // given
        // 지하철_노선_등록되어_있음
        ExtractableResponse createResponse = postRequest(path, line2Request);

        // when
        // 지하철_노선_제거_요청
        String uri = createResponse.header("Location");
        ExtractableResponse<Response> response = delete(uri);

        // then
        // 지하철_노선_삭제됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());

        // and
        // 지하철_노선_삭제확인
        ExtractableResponse<Response> checkResponse = get(uri);
        assertThat(checkResponse.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    private LineRequest 라인2를_생성하기위한_request를_만든다() {
        Map<String, String> params1 = new HashMap<>();
        params1.put("name", "강남역");
        ExtractableResponse<Response> response1 = postRequest("/stations", params1);
        Map<String, String> params2 = new HashMap<>();
        params2.put("name", "역삼역");
        ExtractableResponse<Response> response2 = postRequest("/stations", params2);

        String color2 = "bg-red-600";
        String name2 = "2호선";
        Long upStationId2 = response1.as(StationResponse.class).getId();
        Long downStationId2 = response2.as(StationResponse.class).getId();
        int distance2 = 2;

        return new LineRequest(name2, color2, upStationId2, downStationId2, distance2);
    }

    private LineRequest 라인6을_생성하기위한_request를_만든다() {
        Map<String, String> params3 = new HashMap<>();
        params3.put("name", "태릉입구역");
        ExtractableResponse<Response> response3 = postRequest("/stations", params3);
        Map<String, String> params4 = new HashMap<>();
        params4.put("name", "봉화산역");
        ExtractableResponse<Response> response4 = postRequest("/stations", params4);

        String color6 = "bg-orange-600";
        String name6 = "6호선";
        Long upStationId6 = response3.as(StationResponse.class).getId();
        Long downStationId6 = response4.as(StationResponse.class).getId();
        int distance6 = 6;

        return new LineRequest(name6, color6, upStationId6, downStationId6, distance6);
    }

    private List<String> getNamesFromStations(List<StationResponse> stationResponses) {
        return stationResponses.stream()
                .map(it -> it.getName())
                .collect(Collectors.toList());
    }
}
