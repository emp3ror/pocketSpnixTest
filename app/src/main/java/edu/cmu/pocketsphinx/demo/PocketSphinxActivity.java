/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package edu.cmu.pocketsphinx.demo;

import static android.widget.Toast.makeText;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View.OnTouchListener;


public class PocketSphinxActivity extends Activity implements
        RecognitionListener {
    private static final String DIGITS_SEARCH = "digits";
    public static String TAG ="PocketSphinxVoiceRecognitionService";
    private static final String KWS_SEARCH = "wakeup";
    private static final String FORECAST_SEARCH = "forecast";

    private static final String MENU_SEARCH = "menu";
    private static final String KEYPHRASE = "oh mighty computer";

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;
    public Context context;

    private Button dialer;
    boolean isListening = false;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        dialer = (Button) findViewById(R.id.dialer);

        // Prepare the data for UI
      context = getApplicationContext();
        // Prepare the data for UI
        //Log.i(TAG, "onCreate: setup search options");
        captions = new HashMap<String, Integer>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(MENU_SEARCH, R.string.menu_caption);
        captions.put(DIGITS_SEARCH, R.string.digits_caption);
        captions.put(FORECAST_SEARCH, R.string.forecast_caption);
       setContentView(R.layout.main);
        ((TextView) findViewById(R.id.caption_text))
               .setText("Preparing the recognizer");

        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                  //  Log.i(TAG, "AsyncTask:doInBackground: setup recognizr");
                    Assets assets = new Assets(PocketSphinxActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                   ((TextView) findViewById(R.id.caption_text))
                            .setText("Failed to init recognizer " + result);

                   // Log.e(TAG, "onPostExecute: failed to init recognizer: " + result);
                } else {
                 //   Log.i(TAG, "AsyncTask: onPostExecute: swtich to the digit search");
//                  switchSearch(DIGITS_SEARCH);
                    isListening = true;
                }
            }
        }.execute();



//        dialer.setOnClickListener();
//        dialer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });


    }

    public void clickedDialer(View view) {
        ((TextView) findViewById(R.id.result_text)).setText("hello clicked");
        if (isListening) {
            switchSearch(DIGITS_SEARCH);
        }
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        String text = hypothesis.getHypstr();
       /* if (text.equals(KEYPHRASE))
            switchSearch(MENU_SEARCH);
        else if (text.equals(DIGITS_SEARCH))
            switchSearch(DIGITS_SEARCH);
        else if (text.equals(FORECAST_SEARCH))
            switchSearch(FORECAST_SEARCH);
        else*/
            ((TextView) findViewById(R.id.result_text)).setText(text);
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        ((TextView) findViewById(R.id.result_text)).setText("");
       /* if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
          //  Log.i(TAG, "onResult: " + text);
        }*/
    }

       @Override
      public void onBeginningOfSpeech() {
       }

       @Override
       public void onEndOfSpeech() {
         /*  if (DIGITS_SEARCH.equals(recognizer.getSearchName())
                   || FORECAST_SEARCH.equals(recognizer.getSearchName()))
               switchSearch(DIGITS_SEARCH);*/
         Toast.makeText(context,"end",Toast.LENGTH_LONG).show();
       }

       private void switchSearch(String searchName) {
           recognizer.stop();
           recognizer.startListening(searchName);
           String caption = getResources().getString(captions.get(searchName));
           ((TextView) findViewById(R.id.caption_text)).setText(caption);
       }

    private void setupRecognizer(File assetsDir) {
        File modelsDir = new File(assetsDir, "models");
        recognizer = defaultSetup()
                .setAcousticModel(new File(modelsDir, "hmm/nep_asr_2"))
                .setDictionary(new File(modelsDir, "dict/nep_asr_2.dic"))
                .setRawLogDir(assetsDir).setKeywordThreshold(1e-20f)
                .getRecognizer();
        recognizer.addListener(this);

        // Create keyword-activation search.
      //  recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
        // Create grammar-based searches.
        //File menuGrammar = new File(modelsDir, "grammar/menu.gram");
        //recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
        File digitsGrammar = new File(modelsDir, "grammar/nep_asr_2.gram");
        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);
        // Create language model search.
      //  File languageModel = new File(modelsDir, "lm/nep_asr_2.dmp");
        //recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);
    }
}
