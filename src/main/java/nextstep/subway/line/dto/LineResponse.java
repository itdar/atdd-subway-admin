package nextstep.subway.line.dto;

import nextstep.subway.line.domain.Line;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.dto.StationResponse;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LineResponse {
    private Long id;
    private String name;
    private String color;
    private List<Station> stations = new ArrayList<>();
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    public LineResponse() {
    }

    private LineResponse(Long id, String name, String color,
                         List<Station> stations, LocalDateTime createdDate, LocalDateTime modifiedDate) {
        this.id = id;
        this.name = name;
        this.color = color;

//        for (Station station : stations) {
//            this.stations.add((Station) Hibernate.unproxy(station));
//        }

        // stations 가 List<Station> 일 때, 생성자에서 그대로 넣는 경우 (EAGER)
        this.stations = stations;

        // stations 가 List<Station> 일 때, 생성자에서 복사생성해서 넣는 경우 (LAZY 가능)
//        this.stations = stations.stream()
//                .map(station -> station.clone())
//                .collect(Collectors.toList());

        // stations 가 List<Station> 일 때, 생성자에서 리스트 복사해서 넣는 경우 (EAGER)
//        this.stations = new ArrayList<>(stations);

        // stations 가 List<StationResponse> 일 때, response dto 생성해서 넣는 경우 (LAZY 가능)
//        this.stations = stations.stream()
//                .map(station -> StationResponse.of(station))
//                .collect(Collectors.toList());

        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }

    private LineResponse(Line line) {
        this(line.getId(), line.getName(), line.getColor(), line.getStations(), line.getCreatedDate(), line.getModifiedDate());
    }

    public static LineResponse of(Line line) {
        return new LineResponse(line);
    }

    public static List<LineResponse> listOf(List<Line> lines) {
        return lines.stream()
                .map(line -> LineResponse.of(line))
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public List<Station> getStations() {
        return this.stations;
    }

}
