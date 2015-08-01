package org.xerrard.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.huaqin.wirelessfiletransfer.R;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;

public class UrlUtil {
    private static final Pattern PLAIN_TEXT_TO_ESCAPE = Pattern.compile("[<>&]| {2,}|\r?\n");
    private static final String TAG = "UrlUtil";
    public static  Uri creatFileForSharedContent(Context context, CharSequence shareContent) {
        if (shareContent == null) {
            return null;
        }

        Uri fileUri = null;
        FileOutputStream outStream = null;
        try {
            String fileName = context.getString(R.string.wirelesstransfer_share_file_name) + ".html";
            context.deleteFile(fileName);

            /*
             * Convert the plain text to HTML
             */
            StringBuffer sb = new StringBuffer("<html><head><meta http-equiv=\"Content-Type\""
                    + " content=\"text/html; charset=UTF-8\"/></head><body>");
            // Escape any inadvertent HTML in the text message
            String text = escapeCharacterToDisplay(shareContent.toString());

            // Regex that matches Web URL protocol part as case insensitive.
            Pattern webUrlProtocol = Pattern.compile("(?i)(http|https)://");

            Pattern pattern = Pattern.compile("("
                    + Patterns.WEB_URL.pattern() + ")|("
                    + Patterns.EMAIL_ADDRESS.pattern() + ")|("
                    + Patterns.PHONE.pattern() + ")");
            // Find any embedded URL's and linkify
            Matcher m = pattern.matcher(text);
            while (m.find()) {
                String matchStr = m.group();
                String link = null;

                // Find any embedded URL's and linkify
                if (Patterns.WEB_URL.matcher(matchStr).matches()) {
                    Matcher proto = webUrlProtocol.matcher(matchStr);
                    if (proto.find()) {
                        // This is work around to force URL protocol part be lower case,
                        // because WebView could follow only lower case protocol link.
                        link = proto.group().toLowerCase(Locale.US) +
                                matchStr.substring(proto.end());
                    } else {
                        // Patterns.WEB_URL matches URL without protocol part,
                        // so added default protocol to link.
                        link = "http://" + matchStr;
                    }

                // Find any embedded email address
                } else if (Patterns.EMAIL_ADDRESS.matcher(matchStr).matches()) {
                    link = "mailto:" + matchStr;

                // Find any embedded phone numbers and linkify
                } else if (Patterns.PHONE.matcher(matchStr).matches()) {
                    link = "tel:" + matchStr;
                }
                if (link != null) {
                    String href = String.format("<a href=\"%s\">%s</a>", link, matchStr);
                    m.appendReplacement(sb, href);
                }
            }
            m.appendTail(sb);
            sb.append("</body></html>");

            byte[] byteBuff = sb.toString().getBytes();

            outStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            if (outStream != null) {
                outStream.write(byteBuff, 0, byteBuff.length);
                fileUri = Uri.fromFile(new File(context.getFilesDir(), fileName));
                if (fileUri != null) {
                    Log.d(TAG, "Created one file for shared content: "
                            + fileUri.toString());
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.toString());
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileUri;
    }
    
    /**
     * Escape some special character as HTML escape sequence.
     *
     * @param text Text to be displayed using WebView.
     * @return Text correctly escaped.
     */
    private static String escapeCharacterToDisplay(String text) {
        Pattern pattern = PLAIN_TEXT_TO_ESCAPE;
        Matcher match = pattern.matcher(text);

        if (match.find()) {
            StringBuilder out = new StringBuilder();
            int end = 0;
            do {
                int start = match.start();
                out.append(text.substring(end, start));
                end = match.end();
                int c = text.codePointAt(start);
                if (c == ' ') {
                    // Escape successive spaces into series of "&nbsp;".
                    for (int i = 1, n = end - start; i < n; ++i) {
                        out.append("&nbsp;");
                    }
                    out.append(' ');
                } else if (c == '\r' || c == '\n') {
                    out.append("<br>");
                } else if (c == '<') {
                    out.append("&lt;");
                } else if (c == '>') {
                    out.append("&gt;");
                } else if (c == '&') {
                    out.append("&amp;");
                }
            } while (match.find());
            out.append(text.substring(end));
            text = out.toString();
        }
        return text;
    }
    
}

