package org.hertz;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static int previousIndex = 0;
    private static String[] title = new String[]{"Favorites", "Sequences", "Player", "Settings"};
    private static int[] menuIds = new int[]{R.id.favorites, R.id.sequences, R.id.player, R.id.settings};
    private Fragment[] fragments = new Fragment[4];
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SequencesManager.init(getApplicationContext());

        setContentView(R.layout.activity_main);

        fragments[0] = new FragmentFavorites();
        fragments[1] = new FragmentSequences();
        fragments[2] = new FragmentPlayer();
        fragments[3] = new FragmentSettings();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        if (SequencePlayerService.isPlaying()) {
            bottomNavigationView.setSelectedItemId(menuIds[previousIndex = 2]);
        } else {
            bottomNavigationView.setSelectedItemId(menuIds[previousIndex]);
        }


        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        String rateStr = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        int rate = Integer.parseInt(rateStr);
        SequencePlayerService.setRate(rate);
    }

    private void setCurrentFragment(Fragment fragment, int newIndex) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (previousIndex > newIndex) {
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        previousIndex = newIndex;
        ft.replace(R.id.fragmentFrame, fragment);
        ft.commit();
        setTitle(title[newIndex]);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        CharSequence menuTitle = item.getTitle();
        setTitle(menuTitle);
        for (int i = 0; i < menuIds.length; i++) {
            if (menuIds[i] == item.getItemId()) {
                setCurrentFragment(fragments[i], i);
                break;
            }
        }
        return true;
    }

    @Override
    protected void onResume() {
        setCurrentFragment(fragments[previousIndex], previousIndex);
        bottomNavigationView.setSelectedItemId(menuIds[previousIndex]);
        super.onResume();
    }

    public static void setPreviousIndex(int previousIndex) {
        MainActivity.previousIndex = previousIndex;
    }
}