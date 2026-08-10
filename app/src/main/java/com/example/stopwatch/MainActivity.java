package com.example.stopwatch;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Stopwatch
 *
 * Tracks elapsed time with millisecond precision.
 *
 * Accuracy trick: the displayed time is NEVER built by counting ticks.
 * Every UI update recomputes it from SystemClock.elapsedRealtime()
 * (a monotonic clock that keeps running even while the app is in the
 * background), so the timer cannot drift and keeps correct time across
 * onPause/onResume — and even across screen rotations, because the
 * timing state is saved in onSaveInstanceState.
 *
 * Timing model:
 *   - baseElapsedMs : time accumulated before the current "run"
 *   - startRealtime : SystemClock.elapsedRealtime() when the run began
 *   - elapsed now   = baseElapsedMs + (elapsedRealtime() - startRealtime)
 *     while running; otherwise it is simply baseElapsedMs (frozen).
 */
public class MainActivity extends AppCompatActivity {

    private static final long TICK_MS = 16L; // refresh the display ~60x per second

    private TextView timeHms;      // big HH:MM:SS digits
    private TextView timeMs;       // smaller .mmm millisecond digits
    private TextView stateLabel;   // Stopped / Running / Paused
    private Button startButton;
    private Button pauseButton;
    private Button lapButton;
    private Button resetButton;
    private ListView lapList;
    private ArrayAdapter<String> lapAdapter;
    private final ArrayList<String> lapEntries = new ArrayList<>();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            updateTimeDisplay();
            handler.postDelayed(this, TICK_MS);
        }
    };

    // ---- timing state (restored from the saved instance state) ----
    private long baseElapsedMs = 0L; // accumulated before the current run
    private long startRealtime = 0L; // SystemClock.elapsedRealtime() when the run began
    private boolean running = false;
    private long lastLapMs = 0L;     // elapsed time of the previous lap
    private int lapCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Wire up the views from the XML layout
        timeHms = findViewById(R.id.textView_time);
        timeMs = findViewById(R.id.textView_time_ms);
        stateLabel = findViewById(R.id.textView_state);
        startButton = findViewById(R.id.button_start);
        pauseButton = findViewById(R.id.button_pause);
        lapButton = findViewById(R.id.button_lap);
        resetButton = findViewById(R.id.button_reset);
        lapList = findViewById(R.id.list_laps);

        lapAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lapEntries);
        lapList.setAdapter(lapAdapter);
        lapList.setEmptyView(findViewById(R.id.text_empty));

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { onStartPressed(); }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { onPausePressed(); }
        });
        lapButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { onLapPressed(); }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { onResetPressed(); }
        });

        // Restore state after a configuration change (e.g. screen rotation)
        if (savedInstanceState != null) {
            baseElapsedMs = savedInstanceState.getLong("baseElapsedMs");
            startRealtime = savedInstanceState.getLong("startRealtime");
            running = savedInstanceState.getBoolean("running");
            lastLapMs = savedInstanceState.getLong("lastLapMs");
            lapCount = savedInstanceState.getInt("lapCount");
            ArrayList<String> savedLaps = savedInstanceState.getStringArrayList("laps");
            if (savedLaps != null) {
                lapEntries.clear();
                lapEntries.addAll(savedLaps);
            }
        }

        updateTimeDisplay();
        updateButtonStates();
    }

    // ------------------------------------------------------------------
    //  Lifecycle: keep the stopwatch correct when the app goes
    //  to the background or is rotated.
    // ------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        if (running) {
            startTicking();          // resume refreshing the display
        }
        updateTimeDisplay();
        updateButtonStates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop refreshing the UI, but elapsed time itself keeps counting:
        // it is derived from the monotonic clock, not from ticks.
        stopTicking();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("baseElapsedMs", baseElapsedMs);
        outState.putLong("startRealtime", startRealtime);
        outState.putBoolean("running", running);
        outState.putLong("lastLapMs", lastLapMs);
        outState.putInt("lapCount", lapCount);
        outState.putStringArrayList("laps", lapEntries);
    }

    // ------------------------------------------------------------------
    //  Button actions
    // ------------------------------------------------------------------

    /** Starts the timer from 0, or resumes it from the paused time. */
    private void onStartPressed() {
        if (running) return;

        if (currentElapsedMs() == 0L) {
            // Brand-new start: clear laps from any previous session
            lapEntries.clear();
            lapAdapter.notifyDataSetChanged();
            lapCount = 0;
            lastLapMs = 0L;
        }

        startRealtime = SystemClock.elapsedRealtime();
        running = true;
        startTicking();
        updateButtonStates();
    }

    /** Freezes the timer at the current elapsed time. */
    private void onPausePressed() {
        if (!running) return;
        baseElapsedMs = currentElapsedMs(); // freeze accumulated time
        running = false;
        stopTicking();
        updateTimeDisplay();
        updateButtonStates();
    }

    /** Records the current time into the lap list. */
    private void onLapPressed() {
        if (!running) return;
        long now = currentElapsedMs();
        long delta = now - lastLapMs;       // time since the previous lap
        lapCount++;
        lastLapMs = now;
        // Insert the newest lap at the top so it is always visible
        lapEntries.add(0, String.format(Locale.US,
                "Lap %2d    %s    (+%s)", lapCount, formatFull(now), formatFull(delta)));
        lapAdapter.notifyDataSetChanged();
    }

    /** Stops the timer and resets the display to 00:00:00. */
    private void onResetPressed() {
        running = false;
        stopTicking();
        baseElapsedMs = 0L;
        startRealtime = 0L;
        lastLapMs = 0L;
        lapCount = 0;
        lapEntries.clear();
        lapAdapter.notifyDataSetChanged();
        updateTimeDisplay();
        updateButtonStates();
    }

    // ------------------------------------------------------------------
    //  Time helpers
    // ------------------------------------------------------------------

    /** The true elapsed milliseconds right now. */
    private long currentElapsedMs() {
        if (running) {
            return baseElapsedMs + (SystemClock.elapsedRealtime() - startRealtime);
        }
        return baseElapsedMs;
    }

    /** HH:MM:SS for the big display. */
    private String formatHms(long ms) {
        long h = ms / 3_600_000L;
        long m = (ms / 60_000L) % 60L;
        long s = (ms / 1_000L) % 60L;
        return String.format(Locale.US, "%02d:%02d:%02d", h, m, s);
    }

    /** HH:MM:SS.mmm for the lap list. */
    private String formatFull(long ms) {
        return formatHms(ms) + String.format(Locale.US, ".%03d", ms % 1000L);
    }

    private void updateTimeDisplay() {
        long ms = currentElapsedMs();
        timeHms.setText(formatHms(ms));
        timeMs.setText(String.format(Locale.US, ".%03d", ms % 1000L));
    }

    // ------------------------------------------------------------------
    //  UI state
    // ------------------------------------------------------------------

    private void startTicking() {
        handler.removeCallbacks(ticker);
        handler.post(ticker);
    }

    private void stopTicking() {
        handler.removeCallbacks(ticker);
    }

    /**
     * Enables/disables buttons depending on the state so the user always
     * sees what can be pressed (disabled buttons render greyed out).
     */
    private void updateButtonStates() {
        boolean hasTime = currentElapsedMs() > 0L;

        // Start is greyed out while running; shows "Resume" when paused
        startButton.setEnabled(!running);
        startButton.setText(running ? R.string.start
                : (hasTime ? R.string.resume : R.string.start));

        pauseButton.setEnabled(running);
        lapButton.setEnabled(running);
        resetButton.setEnabled(running || hasTime);

        if (running) {
            stateLabel.setText(R.string.state_running);
            stateLabel.setTextColor(getColor(R.color.state_running));
        } else if (hasTime) {
            stateLabel.setText(R.string.state_paused);
            stateLabel.setTextColor(getColor(R.color.state_paused));
        } else {
            stateLabel.setText(R.string.state_stopped);
            stateLabel.setTextColor(getColor(R.color.state_stopped));
        }
    }
}
