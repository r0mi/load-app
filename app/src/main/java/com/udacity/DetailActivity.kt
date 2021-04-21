package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ActivityDetailBinding

const val STATUS_SUCCESSFUL = 0
const val STATUS_FAILED = 1
const val STATUS_CANCELLED = 2

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        binding.lifecycleOwner = this

        setSupportActionBar(binding.toolbar)

        val downloadId = intent.getIntExtra(DOWNLOAD_ID, -1)
        val downloadRequestId: Long = intent.getLongExtra(DOWNLOAD_REQUEST_ID, -1)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query()
        query.setFilterById(downloadRequestId)
        val cursor = downloadManager.query(query)

        var sizeBytes = getString(R.string.size_bytes, 0)
        val mimeType =
            if (downloadManager.getMimeTypeForDownloadedFile(downloadRequestId).isNullOrEmpty()) {
                getString(R.string.unknown)
            } else {
                downloadManager.getMimeTypeForDownloadedFile(downloadRequestId)
            }

        val description: String
        val downloadStatus = if (cursor.moveToFirst()) {
            val bytes =
                cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            sizeBytes = getString(
                R.string.size_bytes, when (bytes) {
                    -1 -> 0
                    else -> bytes
                }
            )
            description =
                cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION))
            when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_SUCCESSFUL -> STATUS_SUCCESSFUL
                else -> STATUS_FAILED
            }
        } else {
            description = getString(
                when (downloadId) {
                    R.id.glide_radio_button -> R.string.description_glide
                    R.id.loadapp_radio_button -> R.string.description_loadapp
                    R.id.retrofit_radio_button -> R.string.description_retrofit
                    else -> R.string.description_custom
                }
            )
            STATUS_CANCELLED
        }
        binding.contentDetail.downloadStatus = downloadStatus
        binding.contentDetail.textDownloadName.text = description

        binding.contentDetail.textDownloadStatus.text = getString(
            when (downloadStatus) {
                STATUS_SUCCESSFUL -> R.string.status_successful
                STATUS_FAILED -> R.string.status_failed
                else -> R.string.status_cancelled
            }
        )

        binding.contentDetail.textDownloadSize.text = sizeBytes
        binding.contentDetail.textDownloadType.text = mimeType

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(downloadId)

        binding.contentDetail.buttonOk.setOnClickListener {
            finish()
        }
    }

}
