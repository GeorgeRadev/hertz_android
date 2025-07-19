package org.hertz;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

public class FragmentPlayer extends Fragment implements View.OnClickListener {

    static final int REFRESH_INTERVAL = 500;

    public static Sequence playThisSequence = null;
    public static List<String> frequencies = null;
    static Sequence playingSequence = null;
    static int frequencyDuration = 5;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    private TextView playerName;
    private TextView playerNumber;
    private TextView playerDescription;

    private TextView frequencyTime;
    private TextView frequencyRemainingTime;
    private TextView totalTime;
    private TextView totalRemainingTime;

    private ListView frequenciesList;
    private ArrayAdapter frequenciesAdapter;

    private ImageButton prevButton;
    private ImageButton stopButton;
    private ImageButton playPauseButton;
    private ImageButton nextButton;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playerName = (TextView) view.findViewById(R.id.playerName);
        playerNumber = (TextView) view.findViewById(R.id.playerNumber);
        playerDescription = (TextView) view.findViewById(R.id.playerDescription);

        frequencyTime = (TextView) view.findViewById(R.id.frequencyTime);
        frequencyRemainingTime = (TextView) view.findViewById(R.id.frequencyRemainingTime);
        totalTime = (TextView) view.findViewById(R.id.totalTime);
        totalRemainingTime = (TextView) view.findViewById(R.id.totalRemainingTime);

        frequenciesAdapter = new StringAdapter(getActivity(), R.layout.fragment_string_row);
        frequenciesList = (ListView) view.findViewById(R.id.frequenciesList);
        frequenciesList.setAdapter(frequenciesAdapter);

        prevButton = (ImageButton) view.findViewById(R.id.prev);
        prevButton.setOnClickListener(this);
        stopButton = (ImageButton) view.findViewById(R.id.stop);
        stopButton.setOnClickListener(this);
        playPauseButton = (ImageButton) view.findViewById(R.id.play_pause);
        playPauseButton.setOnClickListener(this);
        nextButton = (ImageButton) view.findViewById(R.id.next);
        nextButton.setOnClickListener(this);

        if (playThisSequence != null) {
            frequencyDuration = SequencesManager.getFrequencyDuration(getActivity());

            Sequence s = new Sequence(playThisSequence.name, playThisSequence.description, (playThisSequence.frequencies));
            s.validate();
            frequencies = s.frequenciesList;
            playingSequence = s;
            playThisSequence = null;

            getActivity().startService(new Intent(getContext(), SequencePlayerService.class));

            doBindService();
        }

        frequenciesAdapter.clear();
        if (playingSequence != null) {
            frequenciesAdapter.addAll(frequencies);
        }
        updatePlayIcon();
    }

    void updatePlayIcon() {
        if (SequencePlayerService.isPlaying()) {
            playPauseButton.setImageResource(R.drawable.player_pause);
        } else {
            playPauseButton.setImageResource(R.drawable.player_play);
        }
    }


    void updatePlayer() {
        updatePlayIcon();

        if (playingSequence != null) {
            playerName.setText(playingSequence.name);
            int currentFrequency = SequencePlayerService.getCurrentFrequency();
            List<String> list = playingSequence.frequenciesList;
            playerNumber.setText(list.get(currentFrequency) + "Hz (" + (currentFrequency + 1) + " / " + list.size() + ")");
            playerDescription.setText(playingSequence.description);
            frequenciesAdapter.clear();

            frequencyTime.setText(Sequence.formatSeconds(SequencePlayerService.getCurrentSequenceTime()));
            frequencyRemainingTime.setText("- " + Sequence.formatSeconds(SequencePlayerService.getCurrentRemainingTime()));
            totalTime.setText(Sequence.formatSeconds(SequencePlayerService.getPlayTime()));
            totalRemainingTime.setText("- " + Sequence.formatSeconds(SequencePlayerService.getTotalRemainingTime()));

        } else {
            playerName.setText("N/A");
            playerDescription.setText("--");
            playerNumber.setText("N/A");

            String t = Sequence.formatSeconds(0);
            frequencyTime.setText(t);
            frequencyRemainingTime.setText("- " + t);
            totalTime.setText(t);
            totalRemainingTime.setText("- " + t);

        }
    }

    @Override
    public void onClick(View v) {
        if (v == prevButton) {
            sendMessageToService(SequencePlayerService.PREV, 0, "");
        } else if (v == stopButton) {
            sendMessageToService(SequencePlayerService.STOP, 0, "");
            doUnbindService();
        } else if (v == playPauseButton) {
            if (SequencePlayerService.isPlaying()) {
                sendMessageToService(SequencePlayerService.PAUSE, 0, "");
                playPauseButton.setImageResource(R.drawable.player_pause);
            } else {
                if (messageService == null) {
                    // it was stopped
                    getActivity().startService(new Intent(getContext(), SequencePlayerService.class));
                    doBindService();
                } else {
                    // unpause
                    sendMessageToService(SequencePlayerService.PLAY, 0, "");
                    playPauseButton.setImageResource(R.drawable.player_play);
                }
            }
        } else if (v == nextButton) {
            sendMessageToService(SequencePlayerService.NEXT, 0, "");
        } else if (v == stopButton) {
            sendMessageToService(SequencePlayerService.STOP, 0, "");
        }
    }

    Handler handler = null;
    Runnable runnable = null;

    @Override
    public void onResume() {
        if (playingSequence != null && SequencePlayerService.isPlaying()) {
            doBindService();
        }
        if (handler == null) {
            handler = new Handler();
            runnable = new HeartBeatCallback();
            runnable.run();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            runnable = null;
            handler = null;
        }
        if (playingSequence != null) {
            doUnbindService();
        }
        super.onPause();
    }

    class HeartBeatCallback implements Runnable {
        public void run() {
            updatePlayer();
            handler.postDelayed(runnable, REFRESH_INTERVAL);
        }
    }

    //--------------------------------Service comminication
    static volatile Messenger messageService = null;
    static boolean bounded = false;

    private static final ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            messageService = new Messenger(service);
            if (playingSequence != null) {
                sendMessageToService(SequencePlayerService.SET_AND_PLAY, frequencyDuration, playingSequence);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            playingSequence = null;
            messageService = null;
        }
    };

    void doBindService() {
        if (!bounded) {
            Activity activity = getActivity();
            activity.bindService(new Intent(activity, SequencePlayerService.class), serviceConnection, Context.BIND_AUTO_CREATE);
            bounded = true;
        }
    }

    void doUnbindService() {
        if (bounded) {
            // Detach our existing connection.
            Activity activity = getActivity();
            activity.unbindService(serviceConnection);
            bounded = false;
            messageService = null;
        }
    }

    private static void sendMessageToService(int what, int value, Object data) {
        if (bounded) {
            if (messageService != null) {
                try {
                    Message msg = Message.obtain(null, what, value, 0, data);
                    messageService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}