package com.dlsu.unisync.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dlsu.unisync.R
import com.dlsu.unisync.adapters.SimpleItemAdapter
import com.dlsu.unisync.models.SimpleItem

// Home dashboard with shortcuts and dummy daily updates.
class DashboardFragment : Fragment() {
    private var navigator: DashboardNavigator? = null

    interface DashboardNavigator {
        fun openCrowdMonitoring()
        fun openQrCheckIn()
        fun openNotifications()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigator = context as? DashboardNavigator
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.openCrowdButton).setOnClickListener {
            navigator?.openCrowdMonitoring()
        }
        view.findViewById<Button>(R.id.openQrButton).setOnClickListener {
            navigator?.openQrCheckIn()
        }
        view.findViewById<Button>(R.id.openNotificationsButton).setOnClickListener {
            navigator?.openNotifications()
        }

        val items = listOf(
            SimpleItem("CCPROG3 quiz", "Due today at 5:00 PM"),
            SimpleItem("Library reservation", "Henry Sy discussion room at 3:30 PM"),
            SimpleItem("Campus advisory", "North gate lines are currently light")
        )

        view.findViewById<RecyclerView>(R.id.dashboardRecycler).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SimpleItemAdapter(items)
        }
    }

    override fun onDetach() {
        navigator = null
        super.onDetach()
    }
}
