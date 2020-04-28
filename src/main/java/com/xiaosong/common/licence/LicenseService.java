package com.xiaosong.common.licence;

import com.xiaosong.model.TbLicense;

public class LicenseService {
    public static LicenseService me = new LicenseService();

    public TbLicense findMac(String mac) {
        return TbLicense.dao.findFirst("select * from tb_license where mac = ?",mac);
    }
}
