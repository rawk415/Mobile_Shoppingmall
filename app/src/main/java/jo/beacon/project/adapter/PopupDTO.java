package jo.beacon.project.adapter;


import lombok.Getter;

public class PopupDTO {
    @Getter
    private String productName;
    @Getter
    private String jobj;

    public PopupDTO(String productName, String jobj) {
        this.productName = productName;
        this.jobj = jobj;
    }
}
