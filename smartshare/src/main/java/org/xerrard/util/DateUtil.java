package org.xerrard.util;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

/**
 * 日期工具
 *
 * @author yinshengge
 *
 */
public class DateUtil {

    @SuppressWarnings("deprecation")
    public static Timestamp getMaxDate() {
        Timestamp now = getNow();
        now.setYear(now.getYear() + 100);
        return now;
    }

    public static Timestamp getNow() {
        Timestamp ret = null;

        Calendar c = Calendar.getInstance();
        ret = new Timestamp(c.getTime().getTime());

        return ret;
    }

    @SuppressWarnings("deprecation")
    public static Timestamp getZeroDateTime() {
        return new Timestamp(0, 0, 1, 0, 0, 0, 0);
    }

    @SuppressWarnings("deprecation")
    public static String toDefaultFmtString(Timestamp ts) {

        String ret = "";

        if (ts != null) {
            ret = String.format("%d-%d-%d %d:%d:%d", ts.getYear() + 1900,
                    ts.getMonth() + 1, ts.getDate(), ts.getHours(),
                    ts.getMinutes(), ts.getSeconds());

        }

        return ret;
    }

    public static String toUnderlineFmtString(Timestamp ts) {

        String ret = "";

        if (ts != null) {
            ret = String.format("%d_%d_%d_%d_%d_%d", ts.getYear() + 1900,
                    ts.getMonth() + 1, ts.getDate(), ts.getHours(),
                    ts.getMinutes(), ts.getSeconds());

        }

        return ret;
    }

    /**
     * <p>
     * Description:获得当前时间，如果是今天就显示具体时间，如果不是今天就显示日期
     * <p>
     * @date:2015年4月20日
     * @param ctx
     * @param time
     * @return
     */
    public static String getDateTimeString(Context ctx, long time) {
        Date d = new Date(time);
        CharSequence str = DateUtils.isToday(time) ? DateFormat.getTimeFormat(
                ctx).format(d) : DateFormat.getDateFormat(ctx).format(d);
        return str.toString();
    }

    public static String getNowDateTimeString(Context ctx) {
        return getDateTimeString(ctx, getNow().getTime());
    }
}
