package Telegram;

import DEBUG.Debug;
import WebParser.Parser;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ScheduledPostSender {
    private Timer timer;
    private static List<LocalTime> schedule;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public ScheduledPostSender() {
        this.timer = new Timer();
        schedule = new ArrayList<>();
    }

    public static String getNextPostTime() {
        LocalTime currentTime = LocalTime.now();
        LocalTime nextPostTime = null;
        for (LocalTime postTime : schedule) {
            if (postTime.isAfter(currentTime) && (nextPostTime == null || postTime.isBefore(nextPostTime))) {
                nextPostTime = postTime;
            }
        }
        return nextPostTime != null ? nextPostTime.format(TIME_FORMATTER) : schedule.get(0).format(TIME_FORMATTER);
    }

    public void startScheduledPostSending() {
        schedulePostSending(8, 0);
        schedulePostSending(8, 15);
        schedulePostSending(8, 30);
        schedulePostSending(8, 45);
        schedulePostSending(12, 0);
        schedulePostSending(17, 0);
        schedulePostSending(18, 0);
        schedulePostSending(18, 30);
        schedulePostSending(18, 50);
        schedulePostSending(19, 0);
        schedulePostSending(19, 20);
        schedulePostSending(19, 30);
        schedulePostSending(19, 40);
        schedulePostSending(19, 50);
        schedulePostSending(19, 59);
        schedulePostSending(20, 10);
        schedulePostSending(20, 30);
        schedulePostSending(21, 6);
    }

    private void schedulePostSending(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        Date scheduledTime = calendar.getTime();
        schedule.add(LocalTime.of(hour, minute));

        timer.scheduleAtFixedRate(new ScheduledTask(), scheduledTime, 24 * 60 * 60 * 1000);
    }

    private class ScheduledTask extends TimerTask {
        @Override
        public void run() {
            if(!Debug.isDevBuild) {
                Parser parser = new Parser();
                parser.generateMessageFromXML(0,true);
            }
        }
    }
}
