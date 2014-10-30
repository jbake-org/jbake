package org.jbake.template.jade;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public String format(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

}
