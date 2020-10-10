package iot.ccnu.deviceManagement.mode;

import lombok.Data;

@Data
public class Devices {
    private String uuid;// 订阅主题 （设备校验）
    private String title;
    private String dev_type;
    private Integer status;
    private String user_id;
    private String created;
    private String update;
    private String deviceimg;
    private String info;

}
