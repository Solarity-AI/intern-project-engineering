package com.productreview.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.productreview.app.MainActivity
import com.productreview.app.R

/**
 * Lightweight helper for system-tray notifications with deep link routing.
 *
 * Usage:
 *   NotificationHelper.show(ctx, id = 1, title = "New review",
 *       body = "...", route = Routes.productDetails(productId))
 *
 * Tapping the notification opens MainActivity with the route in extras;
 * MainActivity reads the extra and calls navController.navigate().
 */
object NotificationHelper {

    const val EXTRA_ROUTE = "deep_link_route"

    private const val CHANNEL_ID = "product_review_default"
    private const val CHANNEL_NAME = "Product Review"

    /** Must be called once (idempotent) before showing any notification. */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    /**
     * Post a system notification. When tapped it launches [MainActivity] with
     * [EXTRA_ROUTE] set to [route], which the activity forwards to NavController.
     *
     * @param route  a navigation route string, e.g. Routes.productDetails(id).
     *               Pass null for a plain notification without navigation.
     */
    fun show(
        context: Context,
        id: Int,
        title: String,
        body: String,
        route: String? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            route?.let { putExtra(EXTRA_ROUTE, it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
