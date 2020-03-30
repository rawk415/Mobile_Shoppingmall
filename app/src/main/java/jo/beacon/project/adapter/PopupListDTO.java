package jo.beacon.project.adapter;

import org.json.JSONObject;

import java.io.Serializable;

import lombok.Getter;

public class PopupListDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Getter
    private String productName;
    @Getter
    private String jobj; // JSONObject

    public PopupListDTO(String productName, JSONObject jobj) {
        super();
        this.productName = productName;
        this.jobj = jobj.toString();
    }
}
