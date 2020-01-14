package com.simplicityapp.base.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.simplicityapp.R
import com.simplicityapp.modules.notifications.model.ContentInfo
import com.simplicityapp.modules.places.model.Place
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

        fun methodShare(act: Activity, p: Place) {

            // string to share
            val sharePlace = act.getString(R.string.sharePlace) + " \'"
            val shareLocated = act.getString(R.string.shareLocated) + " "
            val shareApp = act.getString(R.string.shareApp) + ": "
            val shareBody = (sharePlace + p.name + "\'"
                    + "\n" + shareLocated + p.address + "\n\n"
                    + shareApp + getPlayStoreUrl(act))

            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"

            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, act.getString(R.string.app_name))
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
            act.startActivity(Intent.createChooser(sharingIntent, act.getString(R.string.shareUsing)))
        }

        fun methodShareNews(act: Activity, n: ContentInfo) {

            // string to share
            val shareBody = n.title + "\n\n" + getPlayStoreUrl(act)

            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"

            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, act.getString(R.string.app_name))
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            act.startActivity(Intent.createChooser(sharingIntent, "Share Using"))
        }
    }



}