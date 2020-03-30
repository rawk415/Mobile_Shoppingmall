package jo.beacon.project.adapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class RecommendDTO {
    private String text;
    private String url;
//    private String price;
}
