package com.ryosoftware.caffeine_tile

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RadioButton
import android.widget.TextView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TimeoutPickerActivity : AppCompatActivity() {

    private var selectedPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AppSettings.isPreventingScreenOff(this)) {
            finish()
            return
        }

        val values = resources.getIntArray(R.array.predefined_timeouts)
        val defaultTimeout = AppSettings.getDefaultTimeout(this)
        selectedPosition = values.indexOf(defaultTimeout)

        val view = layoutInflater.inflate(R.layout.dialog_timeout_picker, null)
        val listView = view.findViewById<ListView>(R.id.list_choices)
        val checkBox = view.findViewById<MaterialCheckBox>(R.id.chk_default)
        val checkBoxLabel = view.findViewById<TextView>(R.id.chk_default_label)
        val okButton = view.findViewById<MaterialButton>(R.id.btn_ok)

        val adapter = object : BaseAdapter() {
            override fun getCount() = values.size
            override fun getItem(position: Int) = Utils.getLabelForInterval(this@TimeoutPickerActivity, values[position])
            override fun getItemId(position: Int) = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(R.layout.item_timeout_choice, parent, false)
                view.findViewById<TextView>(R.id.text).text = getItem(position)
                val radio = view.findViewById<RadioButton>(R.id.radio)
                radio.setOnCheckedChangeListener(null)
                radio.isChecked = (position == selectedPosition)
                radio.setOnCheckedChangeListener { button, checked ->
                    if (checked) {
                        selectedPosition = position
                        notifyDataSetChanged()
                    }
                }
                view.setOnClickListener {
                    selectedPosition = position
                    notifyDataSetChanged()
                }
                return view
            }
        }

        listView.adapter = adapter
        listView.dividerHeight = 0

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.timeout)
            .setView(view)
            .setOnCancelListener { finish() }
            .setOnDismissListener { finish() }
            .create()

        checkBoxLabel.setOnClickListener {
            checkBox.isChecked = !checkBox.isChecked
        }

        okButton.setOnClickListener {
            if (selectedPosition >= 0) {
                val selected = values[selectedPosition]
                if (checkBox.isChecked) {
                    AppSettings.setDefaultTimeout(this, selected)
                }
                AppSettings.preventScreenOff(this, selected)
            }
            dialog.dismiss()
        }

        dialog.show()
    }
}
