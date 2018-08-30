package com.example.adam.kickon

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class Tools {
    companion object {
        /**
         * get an image from a website and returns it as an Drawable Objekt
         */
        fun loadImageFromUrl(url: String, context: Context): Drawable? {
            try {
                val input = URL(url).getContent() as InputStream
                return Drawable.createFromStream(input, "name")
            } catch (e: Exception) {
                println("-------------------------------")
                e.printStackTrace()
                println("-------------------------------")
                return ContextCompat.getDrawable(context, R.drawable.noimage)
            }
        }

        /**
         * load a JSON String from an url and returns the "results" array as an JSONArray
         */
        fun loadJSONfromUrl(url: String): JSONArray {

            //Do some Network Request
            //val url = "https://lennartkaiser.de/ocr/list_bars_overview.php"
            val obj = URL(url)
            val response = StringBuffer()

            with(obj.openConnection() as HttpURLConnection) {
                // optional default is GET
                requestMethod = "GET"

                println("\nSending 'GET' request to URL : $url")
                println("Response Code : $responseCode")

                BufferedReader(InputStreamReader(inputStream)).use {
                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    println(response.toString())
                }


                val jsonObj = JSONObject(response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1))
                return jsonObj.getJSONArray("results")
            }
        }

        /**
         * gets the bar overview List, creates bar objects and puts them into the "ArrayList<Bar>",
         * which is returned.
         */
        fun getBarList(context: Context): ArrayList<Bar> {
            val barList = ArrayList<Bar>()

            try {
                // Load data
                val bars = loadJSONfromUrl("https://lennartkaiser.de/ocr/list_bars_overview.php")

                // Get Recipe objects from data
                (0 until bars.length()).mapTo(barList) {



                    Bar(bars.getJSONObject(it).getInt("id"),
                            bars.getJSONObject(it).getString("name"),
                            bars.getJSONObject(it).getString("description"),
                            Tools.loadImageFromUrl(bars.getJSONObject(it).getString("logo_url"),
                                    context))
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return barList
        }
    }
}