package dev.flexui.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import dev.flexui.FlexUI

class DemoScreensAdapter(private val activity: FragmentActivity) : FragmentStateAdapter(activity) {
    
    override fun getItemCount(): Int = 5
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DemoFragment.newInstance(DemoFragment.SCREEN_SCRATCH_CARD)
            1 -> DemoFragment.newInstance(DemoFragment.SCREEN_SPIN_RESULT)
            2 -> DemoFragment.newInstance(DemoFragment.SCREEN_QUIZ)
            3 -> DemoFragment.newInstance(DemoFragment.SCREEN_REWARDS)
            4 -> DemoFragment.newInstance(DemoFragment.SCREEN_WIDGET_DEMO)
            else -> DemoFragment.newInstance(DemoFragment.SCREEN_SCRATCH_CARD)
        }
    }
}

class DemoFragment : Fragment() {
    
    companion object {
        const val SCREEN_SCRATCH_CARD = "scratch_card"
        const val SCREEN_SPIN_RESULT = "spin_result"
        const val SCREEN_QUIZ = "quiz"
        const val SCREEN_REWARDS = "rewards"
        const val SCREEN_WIDGET_DEMO = "widget_demo"
        
        private const val ARG_SCREEN_TYPE = "screen_type"
        
        fun newInstance(screenType: String): DemoFragment {
            val fragment = DemoFragment()
            val args = android.os.Bundle()
            args.putString(ARG_SCREEN_TYPE, screenType)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val screenType = arguments?.getString(ARG_SCREEN_TYPE) ?: SCREEN_SCRATCH_CARD
        
        return when (screenType) {
            SCREEN_WIDGET_DEMO -> createWidgetDemoView(inflater, container)
            else -> createFlexUIView(screenType)
        }
    }
    
    private fun createFlexUIView(screenType: String): View {
        val container = FrameLayout(requireContext())
        
        try {
            val jsonConfig = when (screenType) {
                SCREEN_SCRATCH_CARD -> (activity as MainActivity).getScratchCardJson()
                SCREEN_SPIN_RESULT -> (activity as MainActivity).getSpinResultJson()
                SCREEN_QUIZ -> (activity as MainActivity).getQuizJson()
                SCREEN_REWARDS -> (activity as MainActivity).getRewardsDashboardJson()
                else -> (activity as MainActivity).getScratchCardJson()
            }
            
            val view = FlexUI.renderFromJson(requireContext(), jsonConfig)
            if (view != null) {
                container.addView(view)
            } else {
                showError(container, "Failed to render $screenType")
            }
        } catch (e: Exception) {
            showError(container, "Error: ${e.message}")
        }
        
        return container
    }
    
    private fun createWidgetDemoView(inflater: LayoutInflater, container: ViewGroup?): View {
        // Create a view that simulates a native screen with embedded FlexUI widget
        val rootView = LinearLayout(requireContext())
        rootView.orientation = LinearLayout.VERTICAL
        rootView.setBackgroundColor(0xFFF5F5F5.toInt())
        
        // Simulate native toolbar
        val toolbar = TextView(requireContext())
        toolbar.text = "🛒 My Orders"
        toolbar.textSize = 20f
        toolbar.setTextColor(0xFF333333.toInt())
        toolbar.setPadding(48, 48, 48, 24)
        toolbar.setBackgroundColor(0xFFFFFFFF.toInt())
        toolbar.elevation = 4f
        rootView.addView(toolbar)
        
        // Simulate native content
        val nativeContent = TextView(requireContext())
        nativeContent.text = """
            📱 Native Android UI Elements:
            • ActionBar/Toolbar above
            • This TextView (native)
            • RecyclerView below (simulated)
            
            Order #12345 - Delivered ✅
            Order #12346 - In Transit 🚚
            Order #12347 - Processing 📦
        """.trimIndent()
        nativeContent.textSize = 16f
        nativeContent.setTextColor(0xFF666666.toInt())
        nativeContent.setPadding(48, 32, 48, 32)
        rootView.addView(nativeContent)
        
        // Add the FlexUI widget in the middle
        try {
            val widgetJson = (activity as MainActivity).getWidgetDemoJson()
            val flexView = FlexUI.renderFromJson(requireContext(), widgetJson)
            if (flexView != null) {
                rootView.addView(flexView)
            }
        } catch (e: Exception) {
            val errorView = TextView(requireContext())
            errorView.text = "FlexUI Widget Error: ${e.message}"
            errorView.setTextColor(0xFFFF0000.toInt())
            errorView.setPadding(48, 24, 48, 24)
            rootView.addView(errorView)
        }
        
        // More native content
        val moreNativeContent = TextView(requireContext())
        moreNativeContent.text = """
            📱 More native content:
            • Settings button
            • Contact support
            • Rate the app
            
            This shows how FlexUI widgets can be 
            embedded within existing native screens!
        """.trimIndent()
        moreNativeContent.textSize = 14f
        moreNativeContent.setTextColor(0xFF888888.toInt())
        moreNativeContent.setPadding(48, 32, 48, 48)
        rootView.addView(moreNativeContent)
        
        return rootView
    }
    
    private fun showError(container: FrameLayout, message: String) {
        val errorView = TextView(requireContext())
        errorView.text = message
        errorView.setTextColor(0xFFFF0000.toInt())
        errorView.textSize = 16f
        errorView.setPadding(48, 48, 48, 48)
        container.addView(errorView)
    }
}