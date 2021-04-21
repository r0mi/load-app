package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.util.PatternsCompat
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ActivityMainBinding
import com.udacity.util.sendNotification

const val DOWNLOAD_ID = "download_id"
const val DOWNLOAD_REQUEST_ID = "download_request_id"

class MainActivity : AppCompatActivity() {

    private var downloadRequestID: Long = 0
    private var downloadName: String = ""
    private var downloadId: Int = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(binding.toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        binding.contentMain.customButton.setOnClickListener {
            val id = binding.contentMain.downloadSelectionGroup.checkedRadioButtonId
            if (id == -1) {
                Toast.makeText(this, getString(R.string.select_file), Toast.LENGTH_SHORT).show()
                binding.contentMain.customButton.startOnce()
            } else if (id == R.id.custom_radio_button && !PatternsCompat.WEB_URL.matcher(binding.contentMain.customUrlTextInput.text.toString())
                    .matches()
            ) {
                Toast.makeText(this, getString(R.string.input_valid_url), Toast.LENGTH_SHORT).show()
                binding.contentMain.customButton.startOnce()
            } else {
                binding.contentMain.customButton.startIndefinite()
                downloadId = id
                notificationManager.cancel(id)
                when (id) {
                    R.id.glide_radio_button -> download(
                        GLIDE_URL,
                        getString(R.string.title_glide),
                        getString(R.string.description_glide)
                    )
                    R.id.loadapp_radio_button -> download(
                        URL,
                        getString(R.string.title_loadapp),
                        getString(R.string.description_loadapp)
                    )
                    R.id.retrofit_radio_button -> download(
                        RETROFIT_URL,
                        getString(R.string.title_retrofit),
                        getString(R.string.description_retrofit)
                    )
                    else -> download(
                        binding.contentMain.customUrlTextInput.text.toString(),
                        getString(R.string.title_custom),
                        getString(R.string.description_custom)
                    )
                }
            }
        }

        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name),
            getString(R.string.notification_channel_description)
        )
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadRequestID) {
                binding.contentMain.customButton.finish()

                val contentIntent = Intent(applicationContext, DetailActivity::class.java).apply {
                    putExtra(DOWNLOAD_ID, downloadId)
                    putExtra(DOWNLOAD_REQUEST_ID, downloadRequestID)
                }

                pendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    downloadId,
                    contentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                action = NotificationCompat.Action(
                    R.drawable.ic_assistant_black_24dp,
                    applicationContext.getString(R.string.notification_button),
                    pendingIntent
                )

                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query()
                query.setFilterById(downloadRequestID)

                val cursor = downloadManager.query(query)
                val message = getString(
                    if (cursor.moveToFirst()) {
                        when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                            DownloadManager.STATUS_SUCCESSFUL -> R.string.notification_description_success
                            else -> R.string.notification_description_fail
                        }
                    } else {
                        R.string.notification_description_cancel
                    }, downloadName
                )

                notificationManager.sendNotification(
                    message,
                    application,
                    downloadId,
                    pendingIntent,
                    action
                )
            }
        }
    }

    private fun download(
        url: String,
        name: String,
        description: String = getString(R.string.app_description)
    ) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(name)
                .setDescription(description)
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadRequestID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        downloadName = name
    }

    private fun createChannel(channelId: String, channelName: String, channelDescription: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
                .apply {
                    setShowBadge(false)
                }
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.MAGENTA
            notificationChannel.enableVibration(true)
            notificationChannel.description = channelDescription

            notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val GLIDE_URL =
            "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
        private const val RETROFIT_URL =
            "https://github.com/square/retrofit/archive/refs/heads/master.zip"
    }

}
