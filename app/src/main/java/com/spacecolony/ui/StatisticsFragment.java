package com.spacecolony.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.anychart.AnyChart;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.spacecolony.adapter.StatsAdapter;
import com.spacecolony.databinding.FragmentStatisticsBinding;
import com.spacecolony.model.CrewMember;
import com.spacecolony.model.Statistics;
import com.spacecolony.model.StatisticsManager;
import com.spacecolony.model.Storage;
import java.util.ArrayList;
import java.util.List;

/**
 * Statistics screen.
 *
 * Two things happen here:
 *   1. A RecyclerView lists every crew member with their personal
 *      missions-played and victories count.
 *   2. An AnyChart bar chart shows missions-per-crew-member visually.
 *
 * The chart is only built once (in onViewCreated) because rebuilding it
 * on every onResume causes the AnyChart WebView to flicker.  The text
 * summary and the RecyclerView ARE refreshed in onResume so they stay
 * current as crew come and go.
 */
public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private StatsAdapter statsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Colony-wide totals go in the header TextView
        binding.tvColonySummary.setText(StatisticsManager.getInstance().getColonySummary());

        // Set up the per-crew list
        statsAdapter = new StatsAdapter();
        binding.rvCrewStats.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCrewStats.setAdapter(statsAdapter);

        List<CrewMember> everyone = Storage.getInstance().listAllCrew();
        statsAdapter.setCrewList(everyone);

        // Post the chart build so the WebView has time to fully attach to the window.
        // Building it immediately in onViewCreated can crash on some devices.
        final List<CrewMember> crewForChart = everyone;
        binding.anyChartView.post(new Runnable() {
            @Override
            public void run() {
                buildChart(crewForChart);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the text summary and list in case crew gained XP elsewhere
        String freshSummary = StatisticsManager.getInstance().getColonySummary();
        binding.tvColonySummary.setText(freshSummary);

        List<CrewMember> everyone = Storage.getInstance().listAllCrew();
        statsAdapter.setCrewList(everyone);
    }

    /**
     * Builds the AnyChart column (bar) chart.
     *
     * Each bar represents one crew member; the bar height equals
     * the number of missions they have participated in.  If there
     * is nobody yet we drop in a placeholder bar so the chart does
     * not render blank.
     *
     * @param crewList the full roster at the time the fragment loads
     */
    private void buildChart(List<CrewMember> crewList) {
        Cartesian chart = AnyChart.column();

        // Build the data set — one entry per crew member
        List<DataEntry> chartData = new ArrayList<>();
        for (CrewMember cm : crewList) {
            Statistics s = StatisticsManager.getInstance().getStats(cm);

            int missionsDone = 0;
            if (s != null) {
                missionsDone = s.getMissionsPlayed();
            }

            chartData.add(new ValueDataEntry(cm.getName(), missionsDone));
        }

        boolean noOneYet = chartData.isEmpty();
        if (noOneYet) {
            // Placeholder so AnyChart does not crash on an empty data set
            chartData.add(new ValueDataEntry("No crew", 0));
        }

        Column bars = chart.column(chartData);

        // Tooltip that pops up when you tap a bar
        bars.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d)
                .format("{%Value}{groupsSeparator: } missions");

        chart.animation(true);
        chart.title("Missions Per Crew Member");
        chart.yScale().minimum(0d);
        chart.yAxis(0).labels().format("{%Value}{groupsSeparator: }");
        chart.tooltip().positionMode(TooltipPositionMode.POINT);
        chart.interactivity().hoverMode(HoverMode.BY_X);
        chart.xAxis(0).title("Crew Member");
        chart.yAxis(0).title("Missions");

        // Do NOT call setProgressBar(null) — it causes a NullPointerException
        // on devices where the WebView hasn't fully initialised yet.
        binding.anyChartView.setChart(chart);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
