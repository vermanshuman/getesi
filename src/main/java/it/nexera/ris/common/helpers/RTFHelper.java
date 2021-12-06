package it.nexera.ris.common.helpers;

import ca.uhn.hl7v2.hoh.util.IOUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

public class RTFHelper {
// LibreOffice doesn't actually care whether the image really is in PNG format,
// it will accept the image if it is in a supported format.
// Other programs may care however, so it's better to use an image in PNG format.

    public static String pictureFromPngUrl(URL url, Integer wGoal, Integer hGoal) {
        InputStream stream = null;
        String out = "";

        try {
            stream = url.openStream();

            byte[] picture = IOUtils.readInputStreamIntoByteArray(stream);

            out = "{\r\n" +
                    "\\pict\r\n";

            if (wGoal != null)
                out += "\\picwgoal" + wGoal + "\r\n";

            if (hGoal != null)
                out += "\\pichgoal" + hGoal + "\r\n";

            out += "\\pngblip\r\n";

            for (byte b : picture)
                out += String.format("%02x", b);

            out += "\r\n}\r\n";
        } catch (Exception e) {
            e.printStackTrace();
            out = null;
        }

        return out;
    }

    public static boolean makeRtfWithHeaderAndFooter(File inFile,
                                                     File outFile, URL headerImage, Integer headerWGoal,
                                                     Integer headerHGoal, URL footerImage, Integer footerWGoal,
                                                     Integer footerHGoal) {
        try {
            String rtfString =
                    FileUtils.readFileToString(inFile, null);

            int rtfIndex = rtfString.indexOf("\\rtf1", 0);

            if (rtfIndex == -1)
                rtfIndex = rtfString.indexOf("\\rtf", 0);

            if (rtfIndex == -1)
                return false;

            int endOfRtfDenotationIndex1 = rtfString.indexOf("\\", rtfIndex + 4);
            int endOfRtfDenotationIndex2 = rtfString.indexOf('\n', rtfIndex + 4);

            if (endOfRtfDenotationIndex1 == -1 &&
                    endOfRtfDenotationIndex2 == -1)
                return false;

            int endOfRtfDenotationIndex;

            if (endOfRtfDenotationIndex1 == -1)
                endOfRtfDenotationIndex = endOfRtfDenotationIndex2;
            else if (endOfRtfDenotationIndex2 == -1)
                endOfRtfDenotationIndex = endOfRtfDenotationIndex1;
            else
                endOfRtfDenotationIndex = Math.min(endOfRtfDenotationIndex1,
                        endOfRtfDenotationIndex2);

            int bracketCount = 0;
            int endOfRtfIndex = rtfString.indexOf('{');

            do {
                switch (rtfString.charAt(endOfRtfIndex++)) {
                    case '{':
                        bracketCount++;
                        break;
                    case '}':
                        bracketCount--;
                        break;
                }
            } while (bracketCount > 0);

            endOfRtfIndex--;

            String header = (headerImage == null) ? null : ("{\r\n" +
                    "\\header\r\n" +
                    pictureFromPngUrl(headerImage, headerWGoal, headerHGoal) +
                    "\r\n" +
                    "}\r\n");
            String footer = (footerImage == null) ? null : ("{\r\n" +
                    "\\footer\r\n" +
                    pictureFromPngUrl(footerImage, footerWGoal, footerHGoal) +
                    "\r\n" +
                    "}\r\n");

            String newRtfString = rtfString.substring(0, endOfRtfDenotationIndex) +
                    "\r\n" +
                    header +
                    "\r\n" +
                    rtfString.substring(endOfRtfDenotationIndex, endOfRtfIndex) +
                    footer +
                    rtfString.substring(endOfRtfIndex);

            FileUtils.writeStringToFile(outFile, newRtfString, null);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}