package com.beyond.note5.utils;

import android.util.Log;

import com.time.nlp.TimeNormalizer;
import com.time.nlp.TimeUnit;
import com.time.util.DateUtil;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

public class TimeNLPUtil {

    private static TimeNormalizer normalizer;

    public static Date parse(String chineseWithTimeMean) {
        Date date = null;
        try {
            TimeUnit[] timeUnits = getNormalizer().parse(chineseWithTimeMean);
            date = timeUnits[0].getTime();
        } catch (Exception e) {
            Log.i("TimeNLPUtil", e.getMessage());
        }
        return date;
    }

    //明天上午十点开会 -> 明天上午10点
    public static String getTimeExpression(String chineseWithTimeMean) {
        String timeExpression = null;
        try {
            TimeUnit[] timeUnits = getNormalizer().parse(chineseWithTimeMean);
            timeExpression = timeUnits[0].Time_Expression;
        } catch (Exception e) {
            Log.i("TimeNLPUtil", e.getMessage());
        }
        return timeExpression;
    }

    //明天上午十点开会 -> Date
    public static String getTimeNorm(String chineseWithTimeMean) {
        String timeNorm = null;
        try {
            TimeUnit[] timeUnits = getNormalizer().parse(chineseWithTimeMean);
            timeNorm = timeUnits[0].Time_Norm;
        } catch (Exception e) {
            Log.i("TimeNLPUtil", e.getMessage());
        }
        return timeNorm;
    }

    // 明天上午十点开会-> 明天上午十点
    public static String getOriginTimeExpression(String chineseWithTimeMean) {
        String timeNorm = null;
        try {
            TimeUnit[] timeUnits = getNormalizer().parse(chineseWithTimeMean);
            timeNorm = timeUnits[0].Origin_Time_Expression;
        } catch (Exception e) {
            Log.i("TimeNLPUtil", e.getMessage());
        }
        return timeNorm;
    }

    // 明天上午十点开会-> 开会
    public static String getOriginExpressionWithoutTime(String chineseWithTimeMean) {
        String timeNorm = null;
        try {
            TimeUnit[] timeUnits = getNormalizer().parse(chineseWithTimeMean);
            timeNorm = timeUnits[0].Expression_Without_Time_Expression;
            if (!chineseWithTimeMean.startsWith(timeUnits[0].Origin_Time_Expression)
                    && !chineseWithTimeMean.endsWith(timeUnits[0].Origin_Time_Expression)) {
                return null;
            }
        } catch (Exception e) {
            Log.i("TimeNLPUtil", e.getMessage());
        }
        return timeNorm;
    }

    private static TimeNormalizer getNormalizer() {
        if (normalizer == null) {
            normalizer = initNormalizer();
        }
        return normalizer;
    }

    private static TimeNormalizer initNormalizer() {
        URL url = TimeNormalizer.class.getResource("/TimeExp.m");
        TimeNormalizer normalizer = null;
        try {
            normalizer = new TimeNormalizer(url.toURI().toString());
            normalizer.setPreferFuture(true);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return normalizer;
    }


    public static void main(String[] args) {
        URL url = TimeNormalizer.class.getResource("/TimeExp.m");
        TimeNormalizer normalizer = null;
        try {

            String timeExpression = TimeNLPUtil.getTimeExpression("明天上午九点吃饭");
            String timeNorm = TimeNLPUtil.getTimeNorm("明天上午九点吃饭");
            System.out.println(timeExpression);
            System.out.println(timeNorm);

            normalizer = new TimeNormalizer(url.toURI().toString());
            normalizer.parse("Hi，all.下周一下午三点开会");// 抽取时间
            TimeUnit[] unit = normalizer.getTimeUnit();
            System.out.println("Hi，all.下周一下午三点开会");
            System.out.println(DateUtil.formatDateDefault(unit[0].getTime()) + "-" + unit[0].getIsAllDayTime());
            System.out.println(TimeNLPUtil.getOriginExpressionWithoutTime("Hi，all.下周一下午三点开会"));

            normalizer.parse("早上六点起床");// 注意此处识别到6天在今天已经过去，自动识别为明早六点（未来倾向，可通过开关关闭：new TimeNormalizer(classPath+"/TimeExp.m", false)）
            unit = normalizer.getTimeUnit();
            System.out.println("早上六点起床");
            System.out.println(DateUtil.formatDateDefault(unit[0].getTime()) + "-" + unit[0].getIsAllDayTime());

            normalizer.parse("周一开会");// 如果本周已经是周二，识别为下周周一。同理处理各级时间。（未来倾向）
            unit = normalizer.getTimeUnit();
            System.out.println("周一开会");
            System.out.println(DateUtil.formatDateDefault(unit[0].getTime()) + "-" + unit[0].getIsAllDayTime());

            normalizer.parse("下下周一开会");//对于上/下的识别
            unit = normalizer.getTimeUnit();
            System.out.println("下下周一开会");
            System.out.println(DateUtil.formatDateDefault(unit[0].getTime()) + "-" + unit[0].getIsAllDayTime());

            normalizer.parse("6:30 起床");// 严格时间格式的识别
            unit = normalizer.getTimeUnit();
            System.out.println("6:30 起床");
            System.out.println(DateUtil.formatDateDefault(unit[0].getTime()) + "-" + unit[0].getIsAllDayTime());

            normalizer.parse("6-3 春游");// 严格时间格式的识别
            unit = normalizer.getTimeUnit();
            System.out.println("6-3 春游");
            System.out.println(DateUtil.formatDateDefault(unit[0].getTime()) + "-" + unit[0].getIsAllDayTime());

            normalizer.parse("6月3  春游");// 残缺时间的识别 （打字输入时可便捷用户）
            unit = normalizer.getTimeUnit();
            System.out.println("6月3  春游");
            System.out.println(DateUtil.formatDateDefault(unit[0].getTime()) + "-" + unit[0].getIsAllDayTime());

            normalizer.parse("明天早上跑步");// 模糊时间范围识别（可在RangeTimeEnum中修改
            unit = normalizer.getTimeUnit();
            System.out.println("明天早上跑步");
            System.out.println(DateUtil.formatDateDefault(unit[0].getTime()) + "-" + unit[0].getIsAllDayTime());

            normalizer.parse("本周日到下周日出差");// 多时间识别
            unit = normalizer.getTimeUnit();
            System.out.println("本周日到下周日出差");
            System.out.println(DateUtil.formatDateDefault(unit[0].getTime()) + "-" + unit[0].getIsAllDayTime());
            System.out.println(DateUtil.formatDateDefault(unit[1].getTime()) + "-" + unit[1].getIsAllDayTime());

            normalizer.parse("周四下午三点到五点开会");// 多时间识别，注意第二个时间点用了第一个时间的上文
            unit = normalizer.getTimeUnit();
            System.out.println("周四下午三点到五点开会");
            System.out.println(DateUtil.formatDateDefault(unit[0].getTime()) + "-" + unit[0].getIsAllDayTime());
            System.out.println(DateUtil.formatDateDefault(unit[1].getTime()) + "-" + unit[1].getIsAllDayTime());

            //新闻随机抽取长句识别（2016年6月7日新闻,均以当日0点为基准时间计算）
            //例1
            normalizer.parse("昨天上午，第八轮中美战略与经济对话气候变化问题特别联合会议召开。中国气候变化事务特别代表解振华表示，今年中美两国在应对气候变化多边进程中政策对话的重点任务，是推动《巴黎协定》尽早生效。", "2016-06-07-00-00-00");
            unit = normalizer.getTimeUnit();
            System.out.println("昨天上午，第八轮中美战略与经济对话气候变化问题特别联合会议召开。中国气候变化事务特别代表解振华表示，今年中美两国在应对气候变化多边进程中政策对话的重点任务，是推动《巴黎协定》尽早生效。");
            for (int i = 0; i < unit.length; i++) {
                System.out.println("时间文本:" + unit[i].Time_Expression + ",对应时间:" + DateUtil.formatDateDefault(unit[i].getTime()));
            }

            //例2
            normalizer.parse("《辽宁日报》今日报道，6月3日辽宁召开省委常委扩大会，会议从下午两点半开到六点半，主要议题为：落实中央巡视整改要求。", "2016-06-07-00-00-00");
            unit = normalizer.getTimeUnit();
            System.out.println("《辽宁日报》今日报道，6月3日辽宁召开省委常委扩大会，会议从下午两点半开到六点半，主要议题为：落实中央巡视整改要求。");
            for (int i = 0; i < unit.length; i++) {
                System.out.println("时间文本:" + unit[i].Time_Expression + ",对应时间:" + DateUtil.formatDateDefault(unit[i].getTime()));
            }

            //例3
            normalizer.parse("去年11月起正式实施的刑法修正案（九）中明确，在法律规定的国家考试中，组织作弊的将入刑定罪，最高可处七年有期徒刑。另外，本月刚刚开始实施的新版《教育法》中也明确...", "2016-06-07-00-00-00");
            unit = normalizer.getTimeUnit();
            System.out.println("去年11月起正式实施的刑法修正案（九）中明确，在法律规定的国家考试中，组织作弊的将入刑定罪，最高可处七年有期徒刑。另外，本月刚刚开始实施的新版《教育法》中也明确...");
            for (int i = 0; i < unit.length; i++) {
                System.out.println("时间文本:" + unit[i].Time_Expression + ",对应时间:" + DateUtil.formatDateDefault(unit[i].getTime()));
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
