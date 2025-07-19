package org.hertz;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class FragmentSettings extends Fragment implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    private TextView durationText;
    private TextView downloadStatus;
    private SeekBar duration;
    private Button updateButton;
    private CountDownTimer timer;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        durationText = (TextView) view.findViewById(R.id.durationText);
        duration = (SeekBar) view.findViewById(R.id.duration);
        int d = SequencesManager.getFrequencyDuration(getActivity());
        duration.setOnSeekBarChangeListener(this);
        if (d < duration.getMin()) {
            d = duration.getMin();
        }
        duration.setProgress(d);
        updateDurationText(duration, d);

        downloadStatus = (TextView) view.findViewById(R.id.downloadStatus);

        updateButton = (Button) view.findViewById(R.id.updateButton);
        updateButton.setOnClickListener(this);
        updateButtonText();

        // Create and start refresh timer
        timer = new CountDownTimer(Integer.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateButtonText();
            }

            @Override
            public void onFinish() {
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        timer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
    }

    void updateButtonText() {
        downloadStatus.setText(DownoadTask.status);
        updateButton.setText(DownoadTask.downloading ? "downloading..." : "download latest sequences");
        updateButton.setEnabled(!DownoadTask.downloading);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateDurationText(seekBar, progress);
    }

    void updateDurationText(SeekBar seekBar, int progress) {
        durationText.setText("Duration of frequency: " + progress + "secs");
    }

    public void downoadSequences() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getActivity(), "No permission to use internet : enable " + Manifest.permission.INTERNET, Toast.LENGTH_SHORT).show();
        } else {
            new DownoadTask().execute();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int durationInSecconds = duration.getProgress();
        updateDurationText(duration, durationInSecconds);
        SequencesManager.setFrequencyDuration(getActivity(), durationInSecconds);
        SequencePlayerService.setFrequencyDuration(durationInSecconds);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // ok
    }

    @Override
    public void onClick(View v) {
        if (v == updateButton) {
            downoadSequences();
        }
    }
}