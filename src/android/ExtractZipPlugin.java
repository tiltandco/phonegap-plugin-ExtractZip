package org.apache.cordova.plugin.ExtractZip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.apache.cordova.*;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * @author Evgeniy Lukovsky
 * 
 */

/*
	Mearged with code from  Vishal Rajpal to allow for creation of directories that don't exsit 
 	Author: Ryan OConnell
 	Company: Tilt and Co
*/
public class ExtractZipPlugin extends CordovaPlugin {
	public enum Action {
		extract, getTempDir
	}

	/**
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static final void copyInputStream(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[65536];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.cordova.CordovaPlugin#execute(java.lang.String,
	 * org.json.JSONArray, org.apache.cordova.CallbackContext)
	 */
	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		System.out.println("ZIP plugin has been started");
		boolean result = false;

		switch (Action.valueOf(action)) {
		case extract:
			result = true;
			extractAll(args, callbackContext);
			break;
		case getTempDir:
			result = true;
			getTempDir(args, callbackContext);
		}
		return result;
	}

	public static boolean createDirIfNotExists(String path) {
		boolean ret = true;

		File file = new File(Environment.getExternalStorageDirectory(), path);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				Log.e("TravellerLog :: ", "Problem creating folder");
				ret = false;
			}
		}
		return ret;
	}

	/**
	 * @param args
	 * @param callbackContext
	 * @return
	 */
	private boolean extractAll(JSONArray args, CallbackContext callbackContext) {
		// run in background
		cordova.getThreadPool().execute(new Runnable() {
			// TODO Auto-generated method stub
			Log.v("tag", "### ExtractZipFilePlugin");

			Log.v("tag", "### ExtractZipFilePlugin");
			try {
				String filename = args.getString(0);
				String destination = args.getString(1);
				Log.v("tag", "### filename: " + filename);
				Log.v("tag", "### destination: " + destination);
				createDirIfNotExists(destination);
				File file = new File(Environment.getExternalStorageDirectory(),
						filename);
				// String[] dirToSplit=filename.split(File.separator);
				// String dirToInsert="";
				// for(int i=0;i<dirToSplit.length-1;i++)
				// {
				// dirToInsert+=dirToSplit[i]+File.separator;
				// }
				BufferedOutputStream dest = null;
				BufferedInputStream is = null;
				ZipEntry entry;
				ZipFile zipfile;
				try {
					zipfile = new ZipFile(file);
					Enumeration e = zipfile.entries();
					while (e.hasMoreElements()) {
						entry = (ZipEntry) e.nextElement();
						is = new BufferedInputStream(zipfile.getInputStream(entry),
								8192);
						// is = new
						// BufferedInputStream(zipfile.getInputStream(entry));
						int count;
						byte data[] = new byte[102222];
						String fileName = entry.getName();

						String[] dirToSplit = fileName.split(File.separator);
						String dirToInsert = "";
						for (int i = 0; i < dirToSplit.length - 1; i++) {
							dirToInsert += dirToSplit[i] + File.separator;
							createDirIfNotExists(destination + dirToInsert);
						}

						File outFile = new File(
								Environment.getExternalStorageDirectory(),
								destination + fileName);
						if (entry.isDirectory()) {
							outFile.mkdirs();
						} else {
							FileOutputStream fos = new FileOutputStream(outFile);
							dest = new BufferedOutputStream(fos, 102222);
							while ((count = is.read(data, 0, 102222)) != -1) {
								dest.write(data, 0, count);
							}
							dest.flush();
							dest.close();
							is.close();
						}
					}
					Log.v("tag", "### success");
					 callbackContext.success("Succesfully extracted.");
					return true;
				} catch (ZipException e1) {
					// TODO Auto-generated catch block
					Log.v("tag",
							"### MALFORMED_URL_EXCEPTION: "
									+ PluginResult.Status.MALFORMED_URL_EXCEPTION
											.toString());
					callbackContext
							.error(PluginResult.Status.MALFORMED_URL_EXCEPTION
									.toString());
					return false;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					Log.v("tag", "### IO_EXCEPTION: "
							+ PluginResult.Status.IO_EXCEPTION.toString());
					e1.printStackTrace();
					callbackContext.error(PluginResult.Status.IO_EXCEPTION
							.toString());
					return false;
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.v("tag", "### IO_EXCEPTION: "
						+ PluginResult.Status.JSON_EXCEPTION.toString());
				callbackContext
						.error(PluginResult.Status.JSON_EXCEPTION.toString());
				return false;
			}
		});
	}

	private boolean getTempDir(JSONArray args, CallbackContext callbackContext) {
		String dirName;
		try {
			dirName = args.getString(0);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			return false;
		}
		Context appContext = cordova.getActivity().getApplicationContext();
		String absolutePath = appContext.getDir(dirName, Context.MODE_PRIVATE)
				.getAbsolutePath();
		callbackContext.success(absolutePath);
		return true;
	}

}
