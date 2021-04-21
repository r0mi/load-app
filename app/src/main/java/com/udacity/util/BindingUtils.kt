/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.udacity.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.udacity.R
import com.udacity.STATUS_CANCELLED
import com.udacity.STATUS_SUCCESSFUL

/**
 * Set TextView style based on download status
 */
@BindingAdapter("statusStyle")
fun TextView.setStatusStyle(status: Int) {
    setTextAppearance(
        when (status) {
            STATUS_SUCCESSFUL -> R.style.NormalText
            STATUS_CANCELLED -> R.style.WarningText
            else -> R.style.ErrorText
        }
    )
}
