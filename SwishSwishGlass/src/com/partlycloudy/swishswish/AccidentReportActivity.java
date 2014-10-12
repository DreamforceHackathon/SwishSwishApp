package com.partlycloudy.swishswish;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.android.glass.content.Intents;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Toast;

public class AccidentReportActivity extends Activity {
	private static final String TAG = "VoiceDispatcherActivity";

	private static final int SPEECH_REQUEST = 31;
	private static final int TAKE_PICTURE_REQUEST = 32;

	boolean listenForCommand = true;
	boolean pictureCaptureInProgress = false;
	boolean exitInProgress = false;

	StringBuffer currentTextEntry = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentTextEntry = new StringBuffer("");
		displaySpeechRecognizer();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			if (isSuccessfulSpeechResult(requestCode, resultCode)) {
				List<String> results = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				String spokenText = results.get(0);

				if (listenForCommand) {
					if (spokenText.equalsIgnoreCase("picture")) {
						showNotification("adding a picture to the deal ...");
						takePicture();
						pictureCaptureInProgress = true;
					} else if (spokenText.startsWith("comment")) {
						showNotification("writing comment to the deal ...");
						sendTextToSF(spokenText.substring(7));
						listenForCommand = false;
					} else if (spokenText.equalsIgnoreCase("submit")) {
						showNotification("submitting the deal ...");
					} else if (spokenText.equalsIgnoreCase("submit")) {
						showNotification("submitting the deal ...");
					} else if (spokenText.equalsIgnoreCase("done")) {
						finish();
						exitInProgress = true;
					} else {
						showNotification("whoops, did not get that ...");
					}
				} else {
					onTextReady(spokenText);
					listenForCommand = true;
				}
			} else if (isCancelledPictureResult(requestCode, resultCode)) {
				pictureCaptureInProgress = false;
			} else if (isSuccessfulPictureResult(requestCode, resultCode)) {
				pictureCaptureInProgress = false;
				onPictureReady(data);
			}
		} finally {
			try {
				if (!pictureCaptureInProgress && !exitInProgress) {
					displaySpeechRecognizer();
				}
			} catch (Exception e) {
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void sendTextToSF(String text) {
		new SendMessageToSFTask().execute(text);

	}

	private void displaySpeechRecognizer() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		startActivityForResult(intent, SPEECH_REQUEST);
	}

	private boolean isCancelledPictureResult(int requestCode, int resultCode) {
		return requestCode == TAKE_PICTURE_REQUEST
				&& resultCode == RESULT_CANCELED;
	}

	private boolean isSuccessfulPictureResult(int requestCode, int resultCode) {
		return requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK;
	}

	private boolean isSuccessfulSpeechResult(int requestCode, int resultCode) {
		return requestCode == SPEECH_REQUEST && resultCode == RESULT_OK;
	}

	private void onTextReady(String spokenText) {
		currentTextEntry.append(' ').append(spokenText);
		Log.i(TAG,
				"current text recorded buffer:" + currentTextEntry.toString());
	}

	private void onPictureReady(Intent data) {
		String picturePath = data
				.getStringExtra(Intents.EXTRA_PICTURE_FILE_PATH);
		processPictureWhenReady(picturePath);
		showNotification("Picture has been added");
	}

	private void showNotification(CharSequence text) {
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(this, text, duration);
		toast.show();
	}

	private void processPictureWhenReady(String picturePath) {
		// TODO: process picture here
	}

	private void takePicture() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, TAKE_PICTURE_REQUEST);
	}

}