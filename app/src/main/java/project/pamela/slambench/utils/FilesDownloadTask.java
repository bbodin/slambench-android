package project.pamela.slambench.utils;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import project.pamela.slambench.R;
import project.pamela.slambench.SLAMBenchApplication;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */


public class FilesDownloadTask extends AsyncTask<Void, Integer, Boolean> {

    private static final String _url_base = "http://homepages.inf.ed.ac.uk/bbodin";

    private final SLAMBenchApplication _application;
    private final String[] _filename_list;
    private final String[] _md5_list;
    private final Integer[]    _filesize_list;
    private volatile boolean _stop_download = false;
    private final ArrayList<String> _failures;
    private String _progress_message = "starting downloads...";
    private String cache_dir = null;


    /**
     * Constructor (get list of files and corresponding MD5)
     * @param application current application
     * @param filenames list of filenames
     * @param md5 list of md5
     * @param filesizes
     */
    public FilesDownloadTask(Application application, String[] filenames, String[] md5, Integer[] filesizes) {
        super();

        if (application instanceof SLAMBenchApplication) {
                this._application = (SLAMBenchApplication) application;
        } else {
            this._application = null;
        }

        this._filename_list = filenames;
        this._md5_list = md5;
        this._filesize_list = filesizes;
        this._failures = new ArrayList();
        this.cache_dir = application.getCacheDir().getAbsolutePath();
        for (String s : this._filename_list) {
            MessageLog.addDebug("Will download " + s);
        }
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024*8];
        int read;
        int total = 0;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
            total += read;
        }
    }

    /**
     * Copy stream from compressed stream to stream with a specific idea of the size and don't check if the size
     * is verified. check _stop_download value and stop if it happens.
     * @param zipped_in
     * @param out
     * @param expected
     * @return
     * @throws IOException
     */
    private long copyStreamAndUncompressIt(InputStream zipped_in, OutputStream out, int compressed, int expected) throws IOException {
        byte[] buffer = new byte[1024*8];
        int read;
        int total = 0;
        int max_seen = expected;

        int value_to_show    = 0;
        int max_to_show = expected / 1000000;
        //CountInputStream cis = new CountInputStream(zipped_in);
        GZIPInputStream unziped_stream = new GZIPInputStream(zipped_in);

        if (_stop_download) {
            return 0;
        }

        while ((read = unziped_stream.read(buffer)) != -1) {

            out.write(buffer, 0, read);

            total += read;
            if (total > max_seen) max_seen = total;
            if (value_to_show < total / 1000000 ) {
                value_to_show =  total / 1000000;
                this.publishProgress(value_to_show, max_to_show);
            }

            if (_stop_download) {
                Log.d(SLAMBenchApplication.LOG_TAG, "Cancellation of copy+uncompress :" + total);
                return total;
            }

        }
        Log.d(SLAMBenchApplication.LOG_TAG, "End of copy+uncompress :" + total);

        return total;
    }



    /**
     * Copy stream from stream to  stream with a specific idea of the size and don't check if the size
     * is verified. check _stop_download value and stop if it happens.
     * @param in
     * @param out
     * @param expected
     * @return
     * @throws IOException
     */
    private long copyStream(InputStream in, OutputStream out, int expected) throws IOException {
        byte[] buffer = new byte[1024*8];
        int read;
        int total = 0;

        int value_to_show    = 0;
        int max_to_show = expected / 1000000;

        if (_stop_download) {
            return 0;
        }


        while ((read = in.read(buffer)) != -1) {

            out.write(buffer, 0, read);

            total += read;

            if (value_to_show < total / 1000000 ) {
                value_to_show =  total / 1000000;
                this.publishProgress(value_to_show, max_to_show);
            }


            if (_stop_download) {
                Log.d(SLAMBenchApplication.LOG_TAG, "Cancellation of copy :" + total);
                return total;
            }

        }
        Log.d(SLAMBenchApplication.LOG_TAG, "End of copy :" + total);
        return total;
    }


    private String computeMD5(File f) {
        byte[] buffer = new byte[1024*8];
        int read;
        int total_read = 0;
        int file_size = (int)f.length(); // TODO : not safe ?

        int value_to_show    = 0;
        int max_to_show = file_size / 1000000;

        if (_stop_download) {
            return "";
        }


        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            InputStream is = new FileInputStream(f);

            while ((read = is.read(buffer)) > 0) {

                total_read += read;
                digest.update(buffer, 0, read);

                if (value_to_show < total_read / 1000000 ) {
                    value_to_show =  total_read / 1000000;
                    this.publishProgress(value_to_show, max_to_show);
                }


                if (_stop_download) {
                    Log.d(SLAMBenchApplication.LOG_TAG, "Cancellation of MD5 :" + total_read);
                    return "";
                }

            }
            is.close();
            String res = "";
            for (byte v : digest.digest()) {
                res += String.format("%02X", v);
            }

            Log.d(SLAMBenchApplication.LOG_TAG, "End of MD5 :" + total_read);
            return res;
        } catch (Throwable e) {
            Log.d(SLAMBenchApplication.LOG_TAG, "Exception of MD5",e);
            return null;
        }

    }
    /**
     * Given filename, check it's MD5 to be respect
     * @param filename
     * @param md5
     * @return is filename's MD5 == MD5 ?
     */
    private boolean checkMD5(String filename, String md5) {

        File target_file = new File(cache_dir, filename);
        String md5sum = computeMD5(target_file.getAbsoluteFile());
        assert md5sum != null;
        if (!md5sum.equals(md5)) {
            Log.w(SLAMBenchApplication.LOG_TAG, "File MD5 is " + md5sum);

        }
        return md5sum.equals(md5);
    }

    /**
     * Check if filename exists in the Cache
     * @param filename
     * @return
     */
    private boolean checkCache(String filename) {

        File target_file = new File(cache_dir, filename);
        if (target_file.exists()) {
            return true;
        } else {
            Log.w(SLAMBenchApplication.LOG_TAG, String.format( "File %s not found in the cache.", target_file));
        }
        return false;
    }

    /**
     * Copy filename from Assets
     * @param filename
     * @return
     */
    private boolean copyFromAssets(String filename) {

        File target_file = new File(cache_dir, filename);
        int file_size = (int)new File(filename).length(); // TODO : not safe ?
        try {
            InputStream src = _application.getAssets().open(filename);
            OutputStream dst = new FileOutputStream(target_file.getAbsolutePath());
            copyStream(src, dst, file_size);
            src.close();
            dst.flush();
            dst.close();
            return true;


        } catch (java.io.IOException e) {
            Log.w(SLAMBenchApplication.LOG_TAG, e.getMessage());
            Log.w(SLAMBenchApplication.LOG_TAG, String.format( "File %s not found in the assets.", target_file));
        }
        return false;
    }

    /**
     * Download filename from internet (update dialog)
     * @param filename
     * @return success ?
     */
    private boolean copyFromCompressedInternet(String filename,int expected_size) {

        File target_file = new File(cache_dir, filename);


        try {

            URL url = new URL(_url_base + "/" + filename  + ".gz" );
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestProperty("Accept-Encoding", "identity");

            int contentLength = httpConn.getContentLength();
            if (contentLength == -1) {
                Log.e(SLAMBenchApplication.LOG_TAG, "unknown content length for this file");
            } else {
                Log.i(SLAMBenchApplication.LOG_TAG, "content length: " + contentLength + " bytes");
            }

            httpConn.connect();
            InputStream stream = httpConn.getInputStream();
            FileOutputStream out = new FileOutputStream(target_file.getAbsolutePath());
            long realSize = copyStreamAndUncompressIt(stream, out, contentLength, expected_size);
            Log.d(SLAMBenchApplication.LOG_TAG, "Downloaded " + realSize + " bytes");
            if (expected_size == realSize) { // TODO : Find a way to asynchrnously do that (it's fucking long !)
                stream.close();
                out.flush();
                out.close();
            }
            Log.d(SLAMBenchApplication.LOG_TAG, "Close stream.");
            return expected_size == realSize;

        } catch (Throwable e) {
            Log.w(SLAMBenchApplication.LOG_TAG, e.getMessage());
            Log.w(SLAMBenchApplication.LOG_TAG, String.format("File %s not found in the compressed web.", target_file));
        }

        return false;

    }

    /**
     * Download filename from internet (update dialog)
     * @param filename
     * @return success ?
     */
    private boolean copyFromInternet(String filename) {

        File target_file = new File(cache_dir, filename);


        try {

            URL url = new URL(_url_base + "/" + filename);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int contentLength = httpConn.getContentLength();
            if (contentLength == -1) {
                Log.e(SLAMBenchApplication.LOG_TAG, "unknown content length");
            } else {
                Log.i(SLAMBenchApplication.LOG_TAG, "content length: " + contentLength + " bytes");
            }

            httpConn.connect();
            InputStream stream = httpConn.getInputStream();
            FileOutputStream out = new FileOutputStream(target_file.getAbsolutePath());
            long downloadedSize = copyStream(stream, out, contentLength);
            stream.close();
            out.flush();
            out.close();
            if (downloadedSize == contentLength) return true;
        } catch (Throwable e) {
            Log.w(SLAMBenchApplication.LOG_TAG, e.getMessage());
            Log.w(SLAMBenchApplication.LOG_TAG, String.format("File %s not found in the web.", filename));
        }
        return false;

    }

    /**
     * Do whatever it takes to get the correct file in the cache
     * @param filename
     * @param md5
     * @return success ?
     */
    private boolean process(String filename, String md5, int expected_size) {

        /**
         * FIRST : Check cache
         */

        _progress_message = String.format("Look for %s in the cache.",filename);
        if (checkCache(filename)) {
            _progress_message = String.format("Check integrity of %s.",filename);
            if (checkMD5(filename,md5)) {
                return true;
            }
        }

        /**
         * ELSE : Copy From Asset to Cache
         */

        _progress_message = String.format("Look for %s in the assets.",filename);
        if (copyFromAssets(filename)) {

            _progress_message = String.format("Check integrity of %s.",filename);
            _progress_message = "Check integrity of " + filename;
            if (checkMD5(filename,md5)) {
                return true;
            }
        }
        /**
         * ELSE : Copy From compressed network to Cache
         */


        _progress_message = String.format("Download and uncompress %s ...",filename);
        if (copyFromCompressedInternet(filename,expected_size)) {

            _progress_message = String.format("Check integrity of %s.",filename);
            if (checkMD5(filename,md5)) {
                return true;
            }
        }

        /**
         * ELSE : Copy From network to Cache
         */

        if (_stop_download) return false;
        _progress_message = String.format("Download %s ...",filename);
        if (copyFromInternet(filename)) {
            _progress_message = String.format("Check integrity of %s.",filename);
            if (checkMD5(filename,md5)) {
                return true;
            }
        }

        return false;

    }


    /**
     * The background work is the copy/download, at the end it return the failure or sucess
     * @param urls
     * @return success ?
     */
    @Override
    protected Boolean  doInBackground(Void... urls) {

        for (int i = 0 ; i < _filename_list.length ; i++) {
            if (! process (_filename_list[i], _md5_list[i], _filesize_list[i]) ) {
                MessageLog.addDebug("Failed to download file " + _filename_list[i]);
                this._failures.add(_filename_list[i]);
                return false;
            }

        }

        return (this._failures.size() == 0);

    }

    protected void onProgressUpdate(Integer... progress) {

        _stop_download = _application.getCurrentActivity().updateDialog(progress[0], progress[1], _application.getString(R.string.title_preparing_dataset), _progress_message);

    }

    @Override
    protected void onPostExecute(Boolean result) {

            _application.getCurrentActivity().closeDialog();

            MessageLog.addDebug(_application.getString(R.string.debug_end_of_download));
            if (_application.getCurrentActivity() == null) {
                MessageLog.addDebug(_application.getString(R.string.debug_flaw_in_the_system));
            } else if (_application.getCurrentActivity() instanceof FilesDownloadTaskListener) {
                ((FilesDownloadTaskListener)_application.getCurrentActivity()).OnFilesDownloadTaskTerminate(new FileLoadResult(result, this._failures));
            }

    }


    public interface FilesDownloadTaskListener {
        void OnFilesDownloadTaskTerminate(FileLoadResult output);
    }

    public class FileLoadResult {

        public final boolean            success;
        public final ArrayList<String>  failures;

        public FileLoadResult(boolean success,ArrayList<String>  failures) {
            this.success  = success;
            this.failures = failures;
        }
    }
}
