package com.example.team31.ObjectClasses

import android.app.Activity
import android.app.Dialog
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.example.team31.Interfaces.DialogDismissedListener
import com.example.team31.R


class LocationPopupDialog {

    private var dialogDismissed : DialogDismissedListener? = null

    fun showDialog(activity: Activity?, msg: String?) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.pup_up_dialog)
        val text : TextView = dialog.findViewById(R.id.text_dialog)
        text.text = msg
        val dialogButton: Button = dialog.findViewById(R.id.btn_dialog)
        dialogButton.setOnClickListener {
            dialog.dismiss()
            dialogDismissed!!.dialogDismissed()
        }
        dialog.show()
    }

    fun setDialogDismissed(dismissed : DialogDismissedListener){
        dialogDismissed = dismissed
    }


}