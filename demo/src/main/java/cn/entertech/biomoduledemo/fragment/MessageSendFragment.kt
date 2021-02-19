package cn.entertech.biomoduledemo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import cn.entertech.biomoduledemo.R
import cn.entertech.biomoduledemo.utils.getCurrentTime

class MessageSendFragment : Fragment() {

    lateinit var screen: TextView
    lateinit var container: ScrollView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_message_send, container, false)
        initView(view)
        return view
    }

    fun initView(view: View) {
        screen = view.findViewById(R.id.tv_screen)
        container = view.findViewById(R.id.sc_container)
    }


    open fun appendMessageToScreen(message: String) {
        activity?.runOnUiThread {
            screen.append(getCurrentTime() + "-->" + message + "\n")
            container.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    fun clearScreen() {
        screen.setText("")
    }

}
