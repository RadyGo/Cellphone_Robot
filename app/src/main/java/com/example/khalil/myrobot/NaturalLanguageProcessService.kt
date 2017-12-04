package com.example.khalil.myrobot

import ai.api.AIServiceException
import ai.api.RequestExtras
import ai.api.android.AIConfiguration
import ai.api.android.AIDataService
import ai.api.model.*
import android.app.Service
import android.content.Intent
import android.os.AsyncTask
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.text.TextUtils
import android.util.Log


@Suppress("UNREACHABLE_CODE")
/**
 * Created by Michael Wang on 05/28/17.
 * This is for text communication
 */

class NaturalLanguageProcessService : Service() {//AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener {

    //private val gson = GsonFactory.getGson()
    var destination = ""
    var action = ""
    var speech = ""
    var rotation_direction = ""
    var socialmedia = ""
    var phonenumber = ""
    var shape = ""
    var platform = ""
    var message = ""
    private var aiDataService: AIDataService? = null
    var translate_key = ""
    var textprocess: TextProcess? = null

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate() {
        super.onCreate()
        initService()
        Log.d(TAG,"onCreate")

    }

    private fun initService() {
        val selectedLanguage = LanguageConfig("en", "b99aaea780704deb9b455dd830628a37")
        val lang = ai.api.AIConfiguration.SupportedLanguages.fromLanguageTag(selectedLanguage.languageCode)
        val config = AIConfiguration(selectedLanguage.accessToken,
                lang,
                AIConfiguration.RecognitionEngine.System)
        Log.d(TAG,"initservice")
        aiDataService = AIDataService(this,config)
        translate_key = resources.getString(R.string.microsoft_translate_key)
        textprocess = TextProcess(translate_key)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val msg = intent!!.getStringExtra("msg")  // Obtains the SMS.
        message = msg
        phonenumber = intent!!.getStringExtra("phonenumber")
        Log.d(TAG, "onStartCommand: " + phonenumber+ msg)
        // Creates a new thread upon which NavigationService runs.
        val t = Thread(Runnable // Checks if the message is null, if not the service starts.
        {
            if (msg != null) {
                sendRequest(msg)
            }
        })
        t.start()  // The thread starts.
        return Service.START_STICKY
    }

     fun sendRequest(queryString: String,eventString: String = "",contextString: String = "") {

        Log.d(TAG,"send request")
        val task = object : AsyncTask<String, Void, AIResponse>() {

            private var aiError: AIError? = null

            override fun doInBackground(vararg params: String): AIResponse? {
                val request = AIRequest()
                val query = params[0]
                val event = params[1]

                if (!TextUtils.isEmpty(query))
                    request.setQuery(query)
                if (!TextUtils.isEmpty(event))
                    request.setEvent(AIEvent(event))
                val contextString = params[2]
                var requestExtras: RequestExtras? = null
                if (!TextUtils.isEmpty(contextString)) {
                    val contexts = listOf(AIContext(contextString))
                    requestExtras = RequestExtras(contexts, null)
                }

                try {
                    return aiDataService!!.request(request, requestExtras)
                } catch (e: AIServiceException) {
                    aiError = AIError(e)
                    return null
                }

            }

            override fun onPostExecute(response: AIResponse?) {
                if (response != null) {
                    onResult(response)
                } else {
                    Log.d(TAG, "Onresult error.")
                    //onError(aiError!!)
                }
            }
        }

        task.execute(queryString, eventString, contextString)
    }


    // process the messages from the server
    private fun onResult(response: AIResponse) {
        //runOnUiThread {
        Log.d(TAG, "onResult")

        //resultTextView!!.text = gson.toJson(response)

        Log.i(TAG, "Received success response")

        // this is example how to get different parts of result object
        val status = response.status
        Log.i(TAG, "Status code: " + status.code!!)
        Log.i(TAG, "Status type: " + status.errorType)
        //errorTextView.setText("Status type:"+status.errorType)

        val result = response.result
        Log.i(TAG, "Resolved query: " + result.resolvedQuery)

        Log.i(TAG, "Action: " + result.action)
        //actionTextView.setText("Action: "+ result.action)
        action =result.action

        speech = result.fulfillment.speech
        Log.i(TAG, "Speech: " + speech)
        //resultTextView.setText("Speech: "+speech)

        //Speak out
        //TextToSpeechClass.speak(speech)

        val metadata = result.metadata
        if (metadata != null) {
            Log.i(TAG, "Intent id: " + metadata.intentId)
            Log.i(TAG, "Intent name: " + metadata.intentName)
        }

        val params = result.parameters
        var param_String = ""

        if (params != null && !params.isEmpty()) {
            Log.i(TAG, "Parameters: ")
            for ((key, value) in params) {
                param_String= param_String + String.format("%s: %s\n", key, value.toString())
                Log.i(TAG, String.format("%s: %s", key, value.toString()))
                if (key == Commands.NLP_NAVIGATION_DESTINATION) {
                    destination = value.toString()
                }
                if (key == Commands.NLP_TURNAROUND_ROTATION_DIRECTION) {
                    rotation_direction = value.toString()
                }
                if (key == Commands.NLP_PICTURE_TAKING_SOCIAL_MEDIA) {
                    socialmedia = value.toString()
                }
                if (key == Commands.NLP_WALK_SHAPE) {
                    shape = value.toString()
                }
                if (key == Commands.NLP_ACTION_ROBOT) {
                    platform = value.toString()
                }
            }
        }
        sendMessage()
    }

    private fun clear(){
        destination = ""
        action = ""
        speech = ""
        rotation_direction = ""
        socialmedia = ""
        phonenumber = ""
        shape = ""
        platform = ""
    }

    fun sendtoSlack(msg:String, channel:String){
        val i = Intent(this, SlackService::class.java)
        i.putExtra("msg", msg)
        i.putExtra("channelID", channel)
        Log.d(CentralHub.TAG,"Send to slack"+ msg)
        startService(i)
    }

    private fun sendMessage() {
        val intent = Intent("NLP-event")
        Log.d("sender", "Broadcasting message")
        // include some extra data.
        if (destination !="") {
            destination = destination.substring(1,destination.length-1)
            Log.d(TAG,destination+"!!!")
        }
        if (socialmedia !="") {
            socialmedia = socialmedia.substring(1, socialmedia.length - 1)
            Log.d(TAG, socialmedia + "!!!")
        }
        if (rotation_direction != "") {
            rotation_direction = rotation_direction.substring(1, rotation_direction.length - 1)
            Log.d(TAG, rotation_direction + "!!!")
        }
        if (shape != "") {
            shape = shape.substring(1, shape.length - 1)
            Log.d(TAG, shape + "!!!")
        }
        // this is for removing the quotation mark
        if (platform != "") {
            platform = platform.substring(1, platform.length - 1)
            Log.d(TAG, platform + "!!!")
        }

        Log.d(TAG, "Speech:" + speech)

        intent.putExtra(Commands.NLP_ACTION, action)
        intent.putExtra(Commands.NLP_SPEECH,speech)
        intent.putExtra(Commands.ORIGINAL_MESSAGE, message)
        intent.putExtra(Commands.NLP_ACTION_ROBOT, platform)
        intent.putExtra(Commands.NLP_ACTION_CONTACT,phonenumber)

         // Phonenumber could be a slack id number
        if (phonenumber.length ==9 ){
//            AsyncTask.execute { textprocess!!.speak(speech, SpokenDialect.ENGLISH_UNITED_STATES) }
            sendtoSlack(speech,phonenumber)
        }
        else{
//            val sms = CommunicationOut(speech)
//            sms.sendSMS(phonenumber)
        }


        // This is for checking variables
        when (action){
            Commands.NLP_ACTION_NAVIGATION -> if (destination == "") {
                Log.d(TAG,"navigation requires destination")
                return
            }
            else {intent.putExtra(Commands.NLP_NAVIGATION_DESTINATION, destination)}

            Commands.NLP_ACTION_PICTURE_TAKING -> if (socialmedia == ""){
                Log.d(TAG,"picture taking requires social media")
                return
            }
            else {intent.putExtra(Commands.NLP_PICTURE_TAKING_SOCIAL_MEDIA, socialmedia)}

            Commands.NLP_ACTION_TURNAROUND -> if (rotation_direction == ""){
                Log.d(TAG,"Turn around requires rotation_direction")
                return
            }
            else {intent.putExtra(Commands.NLP_TURNAROUND_ROTATION_DIRECTION, rotation_direction)}

            Commands.NLP_ACTION_WALK -> if (shape ==""){
                Log.d(TAG,"Walk requires shape")
                return
            }
            else {intent.putExtra(Commands.NLP_WALK_SHAPE, shape)}
        }


        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        clear()
    }


    companion object {
        val TAG = NaturalLanguageProcessService::class.java.getName()
    }
}
