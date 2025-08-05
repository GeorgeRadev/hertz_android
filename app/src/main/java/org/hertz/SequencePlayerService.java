package org.hertz;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class SequencePlayerService extends Service {

    static final int REFRESH_INTERVAL = 500;

    public static final int DURATION = 0;
    public static final int NAME = 1;
    public static final int FREQUENCIES = 2;

    public static final int PLAY = 3;
    public static final int PAUSE = 4;
    public static final int PREV = 5;
    public static final int NEXT = 6;
    public static final int STOP = 7;
    public static final int SET_AND_PLAY = 8;

    static final int NOTIFICATION_ID = 1024;
    static final String CHANNEL_ID = "Sequence Player Service";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        thiz = this;
        notificationManager = getSystemService(NotificationManager.class);
        Notification notification = getNotification();
        startForeground(NOTIFICATION_ID, notification);
        initPlayer();

        return super.onStartCommand(intent, flags, startId);
    }

    private final Messenger messenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SET_AND_PLAY:
                    setFrequencyDuration(msg.arg1);
                    Sequence s = (Sequence) msg.obj;
                    sequenceName = s.name;
                    sequenceFrequencies = Sequence.frequenciesToDoubles(s.frequencies);
                    playSequence();
                    break;
                case PLAY:
                    playSequence();
                    break;
                case PAUSE:
                    pauseSequence();
                    break;
                case PREV:
                    previousSequence();
                    break;
                case NEXT:
                    nextSequence();
                    break;
                case STOP:
                    stopSequence();
                    stopForeground(true);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private static SequencePlayerService thiz = null;
    private static NotificationManager notificationManager = null;
    private static int frequencyTime = 0;
    private static int sampleRate;

    public static void setRate(int rate) {
        SequencePlayerService.sampleRate = rate;
    }

    public static void setFrequencyDuration(int frequencyDuration) {
        SequencePlayerService.frequencyDuration = 1000 * frequencyDuration;
    }

    public static int getCurrentFrequency() {
        return currentFrequency;
    }

    static Handler timer = null;
    static HeartBeatCallback heartBeatCallback = null;

    static String sequenceName = null;
    static double[] sequenceFrequencies = null;
    static int frequencyDuration = 5000;
    static int currentFrequency = 0;
    static long currentFrequencyTime = 0;
    static long lastExecutionTime = -1;

    public static boolean isPlaying() {
        return timer != null;
    }

    public static int getCurrentSequenceTime() {
        return (int) (currentFrequencyTime / 1000);
    }

    public static int getCurrentRemainingTime() {
        return (int) ((frequencyDuration - currentFrequencyTime) / 1000);
    }

    public static int getPlayTime() {
        if (sequenceFrequencies == null) {
            return 0;
        }
        long payedSequencesTime = frequencyDuration * currentFrequency;
        payedSequencesTime += currentFrequencyTime;
        return (int) (payedSequencesTime / 1000);
    }

    public static int getTotalRemainingTime() {
        if (sequenceFrequencies == null) {
            return 0;
        }
        int remainingTime = frequencyDuration * sequenceFrequencies.length;
        remainingTime = (int) (remainingTime / 1000);
        remainingTime -= getPlayTime();
        return remainingTime;
    }


    static void initPlayer() {
        stopSequence();
    }

    synchronized static void playSequence() {
        if (timer != null) {
            return;
        }
        timer = new Handler();
        heartBeatCallback = new HeartBeatCallback();
        //start progress update
        timer.postDelayed(heartBeatCallback, REFRESH_INTERVAL);
        lastExecutionTime = System.currentTimeMillis();
    }

    static void pauseSequence() {
        if (timer == null) {
            return;
        }
        //pauseSound();
        stopSound();
        timer.removeCallbacksAndMessages(null);
        timer = null;
    }

    static void previousSequence() {
        if (currentFrequency > 0) {
            currentFrequency--;
            currentFrequencyTime = 0;
            lastExecutionTime = System.currentTimeMillis();
            stopSound();
        }
    }

    static boolean nextSequence() {
        if (currentFrequency < sequenceFrequencies.length - 1) {
            currentFrequency++;
            currentFrequencyTime = 0;
            lastExecutionTime = System.currentTimeMillis();
            stopSound();
            return true;
        } else {
            stopSequence();
            return false;
        }
    }

    synchronized static void stopSequence() {
        if (timer != null) {
            timer.removeCallbacksAndMessages(null);
        }
        timer = null;
        stopSound();
        currentFrequency = 0;
        currentFrequencyTime = 0;
    }

    static class HeartBeatCallback implements Runnable {
        public void run() {
            if (progressUpdate()) {
                timer.postDelayed(heartBeatCallback, REFRESH_INTERVAL);
            } else {
                stopSequence();
            }
        }
    }

    static boolean progressUpdate() {
        long now = System.currentTimeMillis();
        // play sequentially every
        if (audioTrack == null) {
            playSound(sequenceFrequencies[currentFrequency]);
        } else {
            currentFrequencyTime += now - lastExecutionTime;
        }
        boolean continuePlaying = true;
        if (currentFrequencyTime > frequencyDuration) {
            // play next frequency
            continuePlaying = nextSequence();
        }

        updateNotification();
        lastExecutionTime = now;
        return continuePlaying;
    }

    private static Notification getNotification() {
        String title = "Hertz: " + sequenceName;
        String message = "";
        if (sequenceFrequencies != null) {
            message = Sequence.formatSeconds(getCurrentSequenceTime()) + " / "
                    + Sequence.formatSeconds(getCurrentRemainingTime()) + "  -  "
                    + sequenceFrequencies[currentFrequency] + " Hz"
                    + " (" + (currentFrequency + 1) + " / " + sequenceFrequencies.length + ")"
                    + "\n"
                    + Sequence.formatSeconds(getPlayTime()) + " / "
                    + Sequence.formatSeconds(getTotalRemainingTime())
                    + "  - total time";
        }

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW);
        notificationChannel.setSound(null,null);
        notificationManager.createNotificationChannel(notificationChannel);
        Notification.Builder notificationBuilder = new Notification.Builder(thiz, CHANNEL_ID)
                .setContentTitle(title)
                //.setContentText(message)
                .setSmallIcon(R.drawable.hertz)
                .setStyle(new Notification.BigTextStyle().bigText(message))
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                //.setContentIntent(pendingIntent)
                //.addAction(new NotificationCompat.Action(R.drawable.player_pause, "Pause", actionSnooze))
                //.addAction(new NotificationCompat.Action(R.drawable.player_play, "Play", actionDismiss))
                .setOngoing(true);
        Notification notification = notificationBuilder.build();
        return notification;
    }

    private static void updateNotification() {
        Notification notification = getNotification();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private static final int durationSeconds = 5;
    private static byte generatedSnd[];
    private static double sample[];

    static void genTone(double freqOfTone) {
        // fill out the array with sin frequency
        final int numSamples = durationSeconds * sampleRate;
        sample = new double[numSamples];
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        // words are stored in two bytes
        generatedSnd = new byte[numSamples << 1];
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
    }

    static AudioTrack audioTrack = null;

    synchronized static void playSound(double freqOfTone) {
        if (audioTrack != null) {
            stopSound();
        }

        genTone(freqOfTone);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        AudioFormat audioFormat = new AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build();

        // then, initialize with new constructor
        AudioTrack at = new AudioTrack(audioAttributes,
                audioFormat,
                generatedSnd.length,
                AudioTrack.MODE_STATIC,
                0);
        at.write(generatedSnd, 0, generatedSnd.length);
        at.setLoopPoints(0, generatedSnd.length >> 1, Integer.MAX_VALUE);
        at.play();
        audioTrack = at;
    }

//    synchronized static void pauseSound() {
//        if (audioTrack != null) {
//            audioTrack.pause();
//        }
//    }

    synchronized static void stopSound() {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }
}