package org.hertz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.util.List;

public class SequenceEdit extends AppCompatActivity {

    EditText sequenceNameEdit;
    EditText sequenceDescriptionEdit;
    EditText frequenciesTextEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sequence_edit);
        setTitle("Edit Sequence");

        Sequence sequence = SequencesManager.getSequenceView();

        sequenceNameEdit = (EditText) findViewById(R.id.sequenceNameEdit);
        sequenceNameEdit.setText(sequence.name);

        sequenceDescriptionEdit = (EditText) findViewById(R.id.sequenceDescriptionEdit);
        sequenceDescriptionEdit.setText(sequence.description);

        frequenciesTextEdit = (EditText) findViewById(R.id.frequenciesTextEdit);
        frequenciesTextEdit.setText(sequence.frequencies.replace(' ', '\n'));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hertz_ok_buton, menu);
        return true;
    }

    boolean validateAndSave() {
        // validate
        String name;
        List<String> list;
        try {

            name = sequenceNameEdit.getText().toString();
            name = name.trim();

            String frequenciesText = frequenciesTextEdit.getText().toString();
            frequenciesText = frequenciesText.trim();

            if (name.length() <= 0 || frequenciesText.length() <= 0) {
                DeleteFragment dialog = new DeleteFragment(SequencesManager.getSequenceView().name);
                dialog.show(getSupportFragmentManager(), "DELETE_DIALOG");
                return false;
            }

            list = Sequence.frequenciesToList(frequenciesText);


            Sequence sequence = SequencesManager.getSequenceView();
            sequence.name = sequenceNameEdit.getText().toString();
            sequence.description = sequenceDescriptionEdit.getText().toString();
            sequence.frequenciesList = list;
            sequence.frequencies = Sequence.frequencesToString(list);
            sequence.validate();

            SequencesManager.sequenceAdd(sequence);

            return true;

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.save) {
            if (validateAndSave()) {
                finish();
                return true;
            }
            return false;
        }
        return super.onOptionsItemSelected(item);

    }


    public static class DeleteFragment extends DialogFragment {
        final String sequenceName;

        DeleteFragment(String name) {
            sequenceName = name;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction.
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("No name or no frequencies will delete the sequence.\nDo you want to delete this sequence ?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            SequencesManager.sequenceDelete(SequencesManager.getSequenceView());
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // nothing
                        }
                    });
            return builder.create();
        }
    }
}