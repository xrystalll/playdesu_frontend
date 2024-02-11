package io.xrystalll.playdesu.util

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import io.xrystalll.playdesu.TAG
import io.xrystalll.playdesu.allGamesUrl
import io.xrystalll.playdesu.data.GameModel
import org.json.JSONObject


class ApiHelper {
    companion object {
        fun getAllGames(
            context: Context,
            list: MutableState<List<GameModel>>,
            isLoading: MutableState<Boolean>,
            noData: MutableState<Boolean>
        ) {
            val queue = Volley.newRequestQueue(context)
            val stringRequest = StringRequest(
                Request.Method.GET,
                allGamesUrl,
                {
                    val filledList = fillGamesList(it)

                    list.value = filledList
                    noData.value = list.value.isEmpty()
                    isLoading.value = false
                },
                {
                    noData.value = true
                    isLoading.value = false
                    Log.d(TAG, "Error: $it")
                }
            )

            queue.add(stringRequest)
        }

        private fun fillGamesList(response: String): List<GameModel> {
            if (response.isEmpty()) return listOf()

            val list = ArrayList<GameModel>()

            val data = JSONObject(response)
            val items = data.getJSONArray("docs")

            for (i in 0 until items.length()) {
                val item = items[i] as JSONObject

                val screenshots = ArrayList<String>()
                val screenshotItems = item.getJSONArray("screenshots")
                for (n in 0 until screenshotItems.length()) {
                    screenshots.add(screenshotItems[n].toString())
                }

                list.add(
                    GameModel(
                        id = item.getString("_id"),
                        displayName = item.getString("displayName"),
                        color = item.getString("color"),
                        description = item.getString("description"),
                        backdrop = item.getString("backdrop"),
                        poster = item.getString("poster"),
                        file = item.getString("file"),
                        studio = item.getString("studio"),
                        gameSystem = item.getString("gameSystem"),
                        releaseYear = item.getString("releaseYear"),
                        genre = item.getString("genre"),
                        price = item.getInt("price"),
                        downloads = item.getInt("downloads"),
                        rating = item.getInt("rating"),
                        size = item.getInt("size"),
                        screenshots = screenshots,
                    )
                )
            }
            return list
        }
    }
}