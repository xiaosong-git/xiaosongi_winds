package com.xiaosong.common;

import com.xiaosong.model.TbAccessrecord;
import io.undertow.servlet.websockets.WebSocketServlet;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Date;

public class MyThread implements Runnable {
    private int sum;
    private int new_sum = 1;
    private boolean stopMe = true;

    public void stopMe() {
        stopMe = false;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {

        WebSocketController wbs = new WebSocketController();
        while (stopMe) {
            TbAccessrecord accessrecord = TbAccessrecord.dao.findFirst("select * from tb_accessrecord where id = ?", +new_sum);
            new_sum = accessrecord.getId();

            wbs.onMessage(new_sum);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
