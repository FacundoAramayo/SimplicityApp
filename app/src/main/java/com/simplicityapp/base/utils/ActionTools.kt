package com.simplicityapp.base.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.simplicityapp.base.config.Constant.LOG_TAG
import com.simplicityapp.modules.places.model.Place
import com.simplicityapp.R
import com.simplicityapp.modules.notifications.model.News
import java.util.*

class ActionTools {

    companion object {
        fun startActivityWithDelay(activity: AppCompatActivity,context: Context, intent: Intent, delay: Long = 2000) {
            val task = object : TimerTask() {
                override fun run() {
                    startActivity(context, intent, null)
                    activity.finish()
                }
            }
            Timer().schedule(task, delay)
        }

        fun rateAction(activity: Activity) {
            val uri = Uri.parse("market://details?id=" + activity.packageName)
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            try {
                activity.startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + activity.packageName)))
            }

        }

        private fun getPlayStoreUrl(act: Activity): String {
            return "http://play.google.com/store/apps/details?id=" + act.packageName
        }

        fun aboutAction(activity: Activity) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(activity.getString(R.string.dialog_about_title))
            builder.setMessage(activity.getString(R.string.about_text))
            builder.setPositiveButton("OK", null)
            builder.show()
        }

        fun dialNumber(ctx: Context, phone: String) {
            try {
                val i = Intent(Intent.ACTION_DIAL)
                i.data = Uri.parse("tel:$phone")
                ctx.startActivity(i)
            } catch (e: Exception) {
                Toast.makeText(ctx, "Cannot dial number", Toast.LENGTH_SHORT).show()
            }
        }

        fun directUrl(ctx: Context, website: String) {
            var url = website
            if (!url.startsWith("https://") && !url.startsWith("http://")) {
                url = "http://$url"
            }
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            ctx.startActivity(i)
        }

        fun methodShare(act: Activity) {
            val shareApp = act.getString(R.string.shareApp) + ": "
            val shareBody = shareApp + getPlayStoreUrl(act)

            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"

            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, act.getString(R.string.app_name))
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
            act.startActivity(Intent.createChooser(sharingIntent, act.getString(R.string.shareUsing)))
        }

        fun methodShare(act: Activity, p: Place) {

            // string to share
            val sharePlace = act.getString(R.string.sharePlace) + " \'"
            val shareLocated = act.getString(R.string.shareLocated) + " "
            val shareApp = act.getString(R.string.findInApp) + ": "
            val shareBody = (sharePlace + p.name + "\'"
                    + "\n" + shareLocated + p.address + ".\n\n"
                    + shareApp + getPlayStoreUrl(act))

            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"

            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, act.getString(R.string.app_name))
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
            act.startActivity(Intent.createChooser(sharingIntent, act.getString(R.string.shareUsing)))
        }

        fun methodShareNews(act: Activity, n: News) {

            // string to share
            val shareApp = act.getString(R.string.findInApp) + ": "
            val shareBody = n.title + "\n\n" + shareApp + getPlayStoreUrl(act)

            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"

            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, act.getString(R.string.app_name))
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            act.startActivity(Intent.createChooser(sharingIntent, act.getString(R.string.shareUsing)))
        }

        fun sendEmail(sendTo: String, subject: String, message: String, act: Activity) {
            /*ACTION_SEND action to launch an email client installed on your Android device.*/
            val mIntent = Intent(Intent.ACTION_SEND)
            /*To send an email you need to specify mailto: as URI using setData() method
            and data type will be to text/plain using setType() method*/
            mIntent.data = Uri.parse("mailto:")
            mIntent.type = "text/plain"
            // put recipient email in intent
            /* recipient is put as array because you may wanna send email to multiple emails
               so enter comma(,) separated emails, it will be stored in array*/
            mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(sendTo))
            //put the Subject in the intent
            mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            //put the message in the intent
            mIntent.putExtra(Intent.EXTRA_TEXT, message)


            try {
                //start email intent
                act.startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
            }
            catch (e: Exception){
                //if any thing goes wrong for example no email client application or any exception
                //get and show exception message
                Log.e(LOG_TAG,"EMAIL_ERROR: " + e.message)
            }

        }
    }



}