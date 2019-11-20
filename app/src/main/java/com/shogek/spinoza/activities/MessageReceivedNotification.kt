package com.shogek.spinoza.activities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore.Images.Media.getBitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import com.shogek.spinoza.R

// More information can be found at:
// https://developer.android.com/design/patterns/notifications.html

/** Helper class for showing and canceling message received notifications. */
object MessageReceivedNotification {
    /** The unique ID for this type of notification. */
    private const val NOTIFICATION_TAG = "SPINOZA_NOTIFICATION_TAG"
    private const val MAIN_CHANNEL = "MAIN_CHANNEL"
    private var isChannelCreated: Boolean = false

    /**
     *  Show a notification when a message is receiver.
     *
     * @param title The title of the notification.
     * @param body The body text of the notification.
     * @param number Show a number. This is useful when stacking notifications of a single type.
     * */
    fun notify(context: Context, title: String, body: String, pictureUri: String?, number: Number) {
        this.createChannel(context)

        // This image is used as the notification's large icon (thumbnail)
        val picture =
            if (pictureUri != null)
                getBitmap(context.contentResolver, Uri.parse(pictureUri))
            else
                null

        val builder = Builder(context, MAIN_CHANNEL)
            // Set appropriate default for the notification light, sound and vibration
            .setDefaults(Notification.DEFAULT_ALL)
            // Set required fields
            .setSmallIcon(R.drawable.ic_notification_24dp)
            .setContentTitle(title)
            .setContentText(body)
            // ----- All fields below this line are optional -----
            // Use a default priority (>= Android 4.1)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Provide a large icon, shown with the notification in the notification drawer (>= Android 3.0)
            .setLargeIcon(picture)
            // Set ticker text (preview) information for this notification.
            .setTicker("$title: $body")
            // Show a number. This is useful when stacking notifications of a single type.
            .setNumber(number.toInt())
            // Automatically dismiss the notification when it is touched.
            .setAutoCancel(true)
            // Remove the words "Now" following the application's name (small icon row)
            .setShowWhen(false)
            // Set the pending intent to be initiated when the user touches the notification
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )

        this.createNotification(context, builder.build())
    }

    fun cancel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(this.NOTIFICATION_TAG, 0)
    }

    private fun createNotification(context: Context, notification: Notification) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(this.NOTIFICATION_TAG, 0, notification)
    }

    private fun createChannel(context: Context) {
        /*
            A channel is used to separate your notifications into categories
            so that the user can disable what he thinks is not important to him
            rather than blocking all notifications from your app.
        */
        if (this.isChannelCreated)
            return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            this.MAIN_CHANNEL,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)

        this.isChannelCreated = true
    }
}