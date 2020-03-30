/**
 *  date: 19.11.02
 *  change public int port = 5000 -> 80
 *  because port 5000 is not opened.
 */

package jo.beacon.project.util;

import lombok.Getter;

public class AddressInfo {
    @Getter
    public String ip = "114.71.137.141"; // Server IP
    @Getter
    public int port = 80; // Server Port
}
