package org.bibledit.android;


import android.app.Activity;
import android.widget.TextView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import org.bibledit.android.MainActivity;
import android.util.Log;
import android.os.Handler;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpStatus;
import java.lang.StringBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpEntity;
import org.bibledit.android.BibleditDirectory;
import android.content.Context;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.File;
import org.apache.http.client.params.ClientPNames;
import java.net.CookiePolicy;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.params.HttpParams;
import org.bibledit.android.Preferences;


public class UpdateGui
{

  private static Integer stage;
  private static String displayedText;

  public static void run () 
  {
    stage = 0;
    displayedText = "";
    display ("Bibledit initializing ...");

    MainActivity.handler = new Handler();

    Runnable runnable = new Runnable () {

      @Override
      public void run() {

        if (stage <= 0) {

          // Test whether the local web server is running and provide feedback as needed.
          Boolean running = serverRunning ();
          if (running) {
            stage++;
          } else {
            StringBuilder stringBuilder = new StringBuilder ();
            stringBuilder.append ("<p>");
            stringBuilder.append ("The web server is offline.");
            stringBuilder.append (" ");
            stringBuilder.append ("Please start the web server.");
            stringBuilder.append ("</p>");
            stringBuilder.append ("<p>");
            stringBuilder.append ("Bibledit relies on a local Android web server app from the Google Play Store.");
            stringBuilder.append ("</p>");
            stringBuilder.append ("<p>");
            stringBuilder.append ("When no web server has been installed yet on this device, install one from Google Play.");
            stringBuilder.append ("</p>");
            stringBuilder.append ("<p>");
            stringBuilder.append ("Install Bit Web Server for production work. This server is able to support heavy use of Bibledit.");
            stringBuilder.append ("</p>");
            stringBuilder.append ("<p>");
            stringBuilder.append ("For trying out Bibledit, the NAMP server app may do that, sort of.");
            stringBuilder.append (" ");
            stringBuilder.append ("In case of errors, use Bit Web Server.");
            stringBuilder.append ("</p>");
            stringBuilder.append ("<p>");
            stringBuilder.append ("Configure the selected web server to listen on localhost port 8080.");
            stringBuilder.append (" ");
            stringBuilder.append ("This is the default setting for most of the Android web servers.");
            stringBuilder.append ("</p>");
            display (stringBuilder.toString ());
            stage = 0;
          }

        } else if (stage == 1) {

          String directory = BibleditDirectory.directoryPalapaWebServer ();
          if (testWebServerDirectory (directory)) {
            InstallAssets.run (directory);
            Preferences.webRoot (directory);
            stage = 9;
          } else {
            stage++;
          }

        } else if (stage == 2) {

          String directory = BibleditDirectory.directoryBitWebServerAndAndroPHP ();
          if (testWebServerDirectory (directory)) {
            InstallAssets.run (directory);
            Preferences.webRoot (directory);
            stage = 9;
          } else {
            stage++;
          }

        } else if (stage == 3) {
          
          String directory = BibleditDirectory.directoryPhpRunnerAndNampAndKsweb ();
          if (testWebServerDirectory (directory)) {
            InstallAssets.run (directory);
            Preferences.webRoot (directory);
            stage = 9;
          } else {
            stage++;
          }

        } else if (stage == 4) {

          String directory = BibleditDirectory.directoryUltiServer ();
          if (testWebServerDirectory (directory)) {
            InstallAssets.run (directory);
            Preferences.webRoot (directory);
            stage = 9;
          } else {
            stage++;
          }

        } else if (stage == 5) {

            StringBuilder stringBuilder = new StringBuilder ();
            stringBuilder.append ("<p>");
            stringBuilder.append ("The web server is running, but Bibledit cannot find the location of its document root.");
            stringBuilder.append ("</p>");
            stringBuilder.append ("<p>");
            stringBuilder.append ("Set the document root of the web server to any of the following locations:");
            stringBuilder.append ("</p>");
            stringBuilder.append ("<p>");
            stringBuilder.append (BibleditDirectory.directoryPalapaWebServer ());
            stringBuilder.append ("</p>");
            stringBuilder.append ("<p>");
            stringBuilder.append (BibleditDirectory.directoryBitWebServerAndAndroPHP ());
            stringBuilder.append ("</p>");
            stringBuilder.append ("<p>");
            stringBuilder.append (BibleditDirectory.directoryPhpRunnerAndNampAndKsweb ());
            stringBuilder.append ("</p>");
            stringBuilder.append ("<p>");
            stringBuilder.append (BibleditDirectory.directoryUltiServer ());
            stringBuilder.append ("</p>");
            display (stringBuilder.toString ());
            stage = 0;

        } else if (stage == 6) {

            stage = 0;

        } else if (stage == 7) {

            stage = 0;

        } else if (stage == 8) {

            stage = 0;

        } else if (stage == 9) {

          // Test whether the Bibledit-Web software is available, and display an Open link if that's the case.
          Boolean available = bibleditAvailable ();
          if (available) {
            displayOpen ();
            stage++;
          } else {
            StringBuilder stringBuilder = new StringBuilder ();
            display ("Bibledit cannot find its scripts.");
            stage = 0;
          }

        } else if (stage == 10) {

        }

        try {
          // Go to the next iteration after a short delay.
          if (stage != 10) {
            MainActivity.handler.postDelayed (this, 300);
          }
        } 
        catch (Exception e) {
        }

      }

    };

    try {
      MainActivity.handler.postDelayed (runnable, 300);
    } catch (Exception e) {
    }


  }


  // Connects to localhost port 8080 and checks for http status 200 (OK).
  // Returns a boolean indicating success.
  private static boolean serverRunning ()
  {
    Boolean running = false;
    DefaultHttpClient client = new DefaultHttpClient ();
    CookieStore cookieStore = new BasicCookieStore ();
    client.setCookieStore(cookieStore);
    CookieSpecFactory csf = new CookieSpecFactory () {
      public CookieSpec newInstance (HttpParams params) {
        return new BrowserCompatSpec () {
          @Override
          public void validate (Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
          }
        };
      }
    };
    client.getCookieSpecs ().register ("easy", csf);
    client.getParams ().setParameter (ClientPNames.COOKIE_POLICY, "easy"); 
    HttpGet httpGet = new HttpGet ("http://localhost:8080");
    try {
      HttpResponse response = client.execute (httpGet);
      Integer statusCode = response.getStatusLine ().getStatusCode ();
      if (statusCode == HttpStatus.SC_OK) running = true;
    } catch (Exception e) {
      running = false;
    }
    return running;
  }
  

  // Connects to bibledit-web at localhost port 8080 and checks for a correct text string.
  // Returns a boolean indicating success.
  private static boolean bibleditAvailable ()
  {
    Boolean available = false;

    DefaultHttpClient client = new DefaultHttpClient ();

    // Our own cookie handler to avoid exceptions in the logcat: Allow all cookies.
    CookieStore cookieStore = new BasicCookieStore ();
    client.setCookieStore(cookieStore);
    CookieSpecFactory csf = new CookieSpecFactory () {
      public CookieSpec newInstance (HttpParams params) {
        return new BrowserCompatSpec () {
          @Override
          public void validate (Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
          }
        };
      }
    };
    client.getCookieSpecs ().register ("easy", csf);
    client.getParams ().setParameter (ClientPNames.COOKIE_POLICY, "easy"); 

    HttpGet httpGet = new HttpGet ("http://localhost:8080/bibledit-web");

    try {
      HttpResponse response = client.execute (httpGet);
      HttpEntity entity = response.getEntity ();
      String string = EntityUtils.toString (entity);
      if (string.contains ("Bibledit")) available = true;
    } catch (Exception e) {
      available = false;
    }

    return available;
  }


  // This puts a known file in a directory passed to the function,
  // and requests this file through the web server.
  // It should get the same file through the web server.
  // It returns a boolean indicating success.
  private static boolean testWebServerDirectory (String directory)
  {
    String filename = directory + "/bibledittest.txt";
    String contents = "Bibledit-Web";
    
    // Save a known content to a known file in the server directory.
    try {
      File file = new File (filename);
      file.createNewFile ();
      FileOutputStream out = new FileOutputStream (file);
      OutputStreamWriter writer = new OutputStreamWriter (out);
      writer.append (contents);
      writer.close ();
      out.close ();
    } catch (Exception e) {
      e.printStackTrace ();
    }

    // Get the file through the web server.      
    Boolean available = false;
    DefaultHttpClient client = new DefaultHttpClient ();
    CookieStore cookieStore = new BasicCookieStore ();
    client.setCookieStore(cookieStore);
    CookieSpecFactory csf = new CookieSpecFactory () {
      public CookieSpec newInstance (HttpParams params) {
        return new BrowserCompatSpec () {
          @Override
          public void validate (Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
          }
        };
      }
    };
    client.getCookieSpecs ().register ("easy", csf);
    client.getParams ().setParameter (ClientPNames.COOKIE_POLICY, "easy"); 
    HttpGet httpGet = new HttpGet ("http://localhost:8080/bibledittest.txt");
    try {
      HttpResponse response = client.execute (httpGet);
      HttpEntity entity = response.getEntity ();
      String string = EntityUtils.toString (entity);
      if (contents.equals (string)) available = true;
    } catch (Exception e) {
      available = false;
    }
    
    if (available) Log.i ("Bibledit-Web root", directory);

    // Delete the file.
    File file = new File (filename);
    file.delete ();

    // Result.
    return available;
  }
    

  private static void display (String html)
  {
    if (html == displayedText) return;
    TextView textView = (TextView) MainActivity.activity.findViewById (R.id.text_view);
    textView.setText (Html.fromHtml (html));
    textView.setMovementMethod (LinkMovementMethod.getInstance ());
    displayedText = html;
  }
  
  
  private static void displayOpen ()
  {
    display ("<big><big><big><a href=\"http://localhost:8080/bibledit-web\">Open Bibledit</a></big></big></big>");
  }


}


/*

Available Android Web Servers that work with Bibledit-Web:
* Web server plus PHP.
* Runs without root access.
* Runs Bibledit-Web well.
* Does not get killed when Android needs memory.

Web Server Ultimate
https://play.google.com/store/apps/details?id=com.icecoldapps.webserverultimate
Web server does not include a PHP interpreter.

Android Webserver
https://play.google.com/store/apps/details?id=com.bolutions.webserver
Source: https::/github.com/bodeme/androidwebserver
It fails to run bibledit-web: On running setup/index.php it gives a blank page.

Server for PHP
https://play.google.com/store/apps/details?id=com.esminis.server.php
http://esminis.com/server.php/
Opening http://192.168.2.8:8080/bibledit-web does not forward it to the correct index.php: That's a problem.
It runs the installer till it cannot find crontab and php-cli: That's good.
It does not include a real web server: That's a problem. It only does one connection at a time.
This is because it runs "php -S". That uses the built-in server.

Servers Ultimate: Trial period of 7 days.
Servers Ultimate Pro: To be purchased.
Install Servers Ultimate Pack C
Requests root access to run.

Web Server (httpd)
Free.
No PHP interpreter.

DroidMote Server
Requires root. 
No PHP.

Struts 2 Web Server Scanner
Not a web server.

PHP Mobile
Small server.
Does not interpret PHP scripts.

PAW Server for Android
Runs on /sdcard/paw
Web pages in: /sdcard/paw/html
http://www.fun2code.de/projects.html
http://paw-android.fun2code.de/
It is a web interface to the Android device, not a PHP server.

kWS Android Web Server
This is not a PHP server.

DroidPHP
Source: https://github.com/droidphp
It has a good web server.
It lacks php-iconv.
Building DroidPHP from source requires dependency available from https://github.com/DroidPHP/DroidPHP#build-dependencies
Import library in Eclipse and add it through Properties -> Android -> add
Cross compiling: https://github.com/DroidPHP/DroidPHP/blob/master/docs/Compiling.md

DroidPHP++
https://github.com/droidphp
It has a good web server.
It lacks php-iconv.

*/
