package org.hertz;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SequenceView extends AppCompatActivity implements View.OnClickListener {

    private ImageButton favoriteButton;
    private Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sequence_view);
        setTitle("View Sequence");

        Sequence sequence = SequencesManager.getSequenceView();

        if (sequence != null) {

            TextView sequenceName = (TextView) findViewById(R.id.sequenceName);
            sequenceName.setText(sequence.name);

            TextView sequenceDescription = (TextView) findViewById(R.id.sequenceDescription);
            sequenceDescription.setText(sequence.description);

            StringAdapter frequenciesAdapter = new StringAdapter(this, R.layout.fragment_string_row);
            ListView frequenciesList = (ListView) findViewById(R.id.frequenciesList);
            frequenciesList.setAdapter(frequenciesAdapter);
            frequenciesAdapter.clear();
            frequenciesAdapter.addAll(sequence.frequenciesList);

            favoriteButton = (ImageButton) findViewById(R.id.favoriteButton);
            favoriteButton.setOnClickListener(this);
            if (SequencesManager.isFavorite(sequence)) {
                favoriteButton.setAlpha(1f);
            } else {
                favoriteButton.setAlpha(0.2f);
            }
            playButton = (Button) findViewById(R.id.buttonPlay);
            playButton.setOnClickListener(this);

        } else {
            playButton = (Button) findViewById(R.id.buttonPlay);
            playButton.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hertz_edit_buton, menu);
        return true;
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onClick(View v) {
        if (v == favoriteButton) {
            Sequence sequence = SequencesManager.getSequenceView();
            if (SequencesManager.isFavorite(sequence)) {
                // remove from favorites
                SequencesManager.favoriteRemove(sequence);
                favoriteButton.setAlpha(0.2f);
            } else {
                // add to favorites
                SequencesManager.favoriteAdd(sequence);
                favoriteButton.setAlpha(1f);
            }
            return;
        }
        if (v == playButton) {
            Sequence sequence = SequencesManager.getSequenceView();
            FragmentPlayer.playThisSequence = sequence;
            MainActivity.setPreviousIndex(2);
            finish();
            return;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.edit) {
            finish();
            Intent intent = new Intent(this, SequenceEdit.class);
            this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}