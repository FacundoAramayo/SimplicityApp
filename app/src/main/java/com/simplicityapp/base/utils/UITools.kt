package com.simplicityapp.base.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.ActionBar
import com.simplicityapp.R
import com.simplicityapp.base.config.SharedPref
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade


class UITools {

    companion object {

        fun setActionBarColor(ctx: Context, actionbar: ActionBar) {
            val colordrw = ColorDrawable(
                SharedPref(
                    ctx
                ).themeColorInt)
            actionbar.setBackgroundDrawable(colordrw)
        }

        fun displayImage(ctx: Context, img: ImageView, url: String) {
            try {
                Glide.with(ctx.applicationContext).load(url)
                        .transition(withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(img)
            } catch (e: Exception) {
            }
        }

        fun displayImageThumb(ctx: Context, img: ImageView, url: String, thumb: Float) {
            try {
                Glide.with(ctx.applicationContext).load(url)
                        .transition(withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .thumbnail(thumb)
                        .into(img)
            } catch (e: Exception) {
            }

        }

        fun clearImageCacheOnBackground(ctx: Context) {
            try {
                Thread(Runnable { Glide.get(ctx).clearDiskCache() }).start()
            } catch (e: Exception) {
            }

        }

        fun getGridSpanCount(activity: Activity): Int {
            val display = activity.windowManager.defaultDisplay
            val displayMetrics = DisplayMetrics()
            display.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels.toFloat()
            val cellWidth = activity.resources.getDimension(R.dimen.item_place_width)
            return Math.round(screenWidth / cellWidth)
        }

        fun createBitmapFromView(act: Activity, view: View): Bitmap {
            val displayMetrics = DisplayMetrics()
            act.windowManager.defaultDisplay.getMetrics(displayMetrics)

            view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
            view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
            view.buildDrawingCache()
            val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)
            view.draw(canvas)

            return bitmap
        }

        fun createBitmapFromView(view: View, width: Int, height: Int): Bitmap {
            val statusBarHeight = dpToPx(24)
            if (width > 0 && height > 0) {
                view.measure(View.MeasureSpec.makeMeasureSpec(dpToPx(width), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(dpToPx(height), View.MeasureSpec.EXACTLY))
            }
            view.layout(0, statusBarHeight, view.measuredWidth, view.measuredHeight + statusBarHeight)

            val bitmap = Bitmap.createBitmap(
                view.measuredWidth,
                view.measuredHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            val background = view.background

            background?.draw(canvas)
            view.draw(canvas)

            return bitmap;
        }

        fun colorDarker(color: Int): Int {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[2] *= 0.8f // value component
            return Color.HSVToColor(hsv)
        }

        fun colorBrighter(color: Int): Int {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[2] /= 0.8f // value component
            return Color.HSVToColor(hsv)
        }


        fun dpToPx(dp: Int): Int {
            val r = Resources.getSystem()
            return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics))
        }


    }
}